package com.coralclubes.facil.modules.cobranza.engines.cupones.strategies.beneficios;

import com.coralclubes.facil.modules.cobranza.dto.response.CuponBeneficioResponse;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponAccionInstruccion;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponEvaluacionContexto;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.ResultadoAplicacionBeneficio;
import com.coralclubes.facil.modules.cobranza.engines.cupones.interfaces.CuponBeneficioStrategy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Estrategia para el beneficio "Paga X y Obtén Y" (X_POR_Y).
 *
 * <ol>
 *   <li><b>Extracción de configuración del beneficio:</b>
 *     Lee los valores {@code paga} ($X$) y {@code recibe} ($Y$) configurados en el cupón
 *     (soporta formato JSON {@code {"paga": 2, "recibe": 2}}, {@code {"paga": 1, "obten": 2}}
 *     o textual {@code "2x2"}, {@code "1,2"}).
 *   </li>
 *   <li><b>Validación del mínimo de elementos requeridos:</b>
 *     Evalúa en el {@link CuponEvaluacionContexto} la cantidad de elementos que el usuario está pagando
 *     (busca en atributos {@code cantidadPagada}, {@code cantidad} o {@code noches}).
 *     Si dicha cantidad no está presente o es menor que el mínimo requerido ({@code cantidadPagada < paga}),
 *     el beneficio <b>no es válido para aplicar</b> y retorna {@link ResultadoAplicacionBeneficio#noAplica()}.
 *   </li>
 *   <li><b>Diferenciación de casos según la cantidad de elementos pagados:</b>
 *     <ul>
 *       <li><b>Caso 1 (El usuario paga exactamente el mínimo, {@code cantidadPagada == paga}):</b><br>
 *         El usuario no tiene elementos adicionales pre-cargados en su orden o reservación.
 *         Por ende, no se descuenta dinero de lo ya pagado (<b>montoDescuento = 0</b>).
 *         Se emite una instrucción de acción de tipo {@code "BONIFICACION_UNIDADES_POSTERIOR"}
 *         con la {@code cantidadARecibir} para que el módulo consumidor (p. ej. Reservaciones)
 *         otorgue o amplíe los elementos prometidos en el postprocesamiento.
 *       </li>
 *       <li><b>Caso 2 (El usuario paga más elementos de los que debía pagar, {@code cantidadPagada > paga}):</b><br>
 *         El usuario ya incluyó en su reservación o carrito los elementos bonificados.
 *         Se calcula el costo unitario dividiendo el {@code montoOriginal} entre la {@code cantidadPagada}:
 *         <pre>costoUnitario = montoOriginal / cantidadPagada</pre>
 *         El descuento aplicable corresponde a las unidades bonificadas:
 *         <pre>montoDescuento = costoUnitario * min(cantidadARecibir, cantidadPagada - paga)</pre>
 *         Se emite una instrucción de acción diferenciada de tipo {@code "DESCUENTO_UNIDADES_INCLUIDAS"}
 *         indicando el monto descontado y las unidades absorbidas.
 *       </li>
 *     </ul>
 *   </li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BeneficioXPorYStrategy implements CuponBeneficioStrategy {

    public static final String CLAVE = "X_POR_Y";

    public static final String ACCION_BONIFICACION_POSTERIOR = "BONIFICACION_UNIDADES_POSTERIOR";
    public static final String ACCION_DESCUENTO_INCLUIDAS = "DESCUENTO_UNIDADES_INCLUIDAS";

    private final ObjectMapper objectMapper;

    @Override
    public String getClaveBeneficio() {
        return CLAVE;
    }

    @Override
    public ResultadoAplicacionBeneficio aplicar(CuponBeneficioResponse beneficio, CuponEvaluacionContexto contexto) {
        String configStr = beneficio.configuracionBeneficio();
        int paga = 1;
        int recibe = 2;

        if (configStr != null && !configStr.isBlank()) {
            try {
                if (configStr.trim().startsWith("{")) {
                    Map<String, Object> map = objectMapper.readValue(configStr, new TypeReference<>() {
                    });
                    if (map.containsKey("paga")) {
                        paga = ((Number) map.get("paga")).intValue();
                    }
                    if (map.containsKey("recibe")) {
                        recibe = ((Number) map.get("recibe")).intValue();
                    } else if (map.containsKey("obten")) {
                        recibe = ((Number) map.get("obten")).intValue();
                    }
                } else if (configStr.contains("x") || configStr.contains("X")) {
                    String[] partes = configStr.split("[xX]");
                    paga = Integer.parseInt(partes[0].trim());
                    recibe = Integer.parseInt(partes[1].trim());
                } else if (configStr.contains(",")) {
                    String[] partes = configStr.split(",");
                    paga = Integer.parseInt(partes[0].trim());
                    recibe = Integer.parseInt(partes[1].trim());
                }
            } catch (Exception e) {
                log.warn("No se pudo parsear configuracion de x_por_y: '{}', usando paga=1, recibe=2", configStr, e);
            }
        }

        // Cantidad de elementos que se deben recibir como beneficio
        // Si recibe > paga (ej: paga 1 recibe 2 totales -> beneficio neto = 1)
        // Si recibe <= paga (ej: {"paga": 2, "recibe": 2} donde recibe 2 adicionales -> beneficio = 2)
        int cantidadARecibir = Math.max(1, recibe > paga ? (recibe - paga) : recibe);

        // 1. Obtener la cantidad que el usuario está pagando/consumiendo
        Integer cantidadPagada = contexto.getAtributo("cantidadPagada", Integer.class)
                .or(() -> contexto.getAtributo("cantidad", Integer.class))
                .or(() -> contexto.getAtributo("noches", Integer.class))
                .orElse(null);

        // Si no se especifica la cantidad o no cumple el mínimo requerido: no es válido para aplicar
        if (cantidadPagada == null || cantidadPagada < paga) {
            log.info("Beneficio X_POR_Y no aplica: cantidadPagada ({}) es menor al mínimo requerido paga ({})",
                    cantidadPagada, paga);
            return ResultadoAplicacionBeneficio.noAplica();
        }

        String conceptoObjetivo = beneficio.conceptoClave();
        BigDecimal montoOriginal = contexto.montoOriginal() != null ? contexto.montoOriginal() : BigDecimal.ZERO;

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("paga", paga);
        metadata.put("recibe", recibe);
        metadata.put("cantidadARecibir", cantidadARecibir);
        metadata.put("cantidadPagada", cantidadPagada);
        metadata.put("conceptoId", beneficio.conceptoId());
        metadata.put("conceptoClave", beneficio.conceptoClave());
        metadata.put("beneficioId", beneficio.beneficioId());

        // 2. Si la cantidad de elementos que pagó es mayor a la que debía pagar (cantidadPagada > paga)
        if (cantidadPagada > paga) {
            // Unidades excedentes que ya están en la orden y pueden ser bonificadas
            int unidadesBonificables = Math.min(cantidadARecibir, cantidadPagada - paga);

            // Divide el monto original entre la cantidad de elementos pagados para determinar el costo unitario
            BigDecimal costoUnitario = montoOriginal.divide(BigDecimal.valueOf(cantidadPagada), 4, RoundingMode.HALF_UP);
            BigDecimal montoDescuento = costoUnitario.multiply(BigDecimal.valueOf(unidadesBonificables)).setScale(2, RoundingMode.HALF_UP);

            metadata.put("costoUnitarioCalculado", costoUnitario);
            metadata.put("unidadesDescontadas", unidadesBonificables);

            CuponAccionInstruccion instruccion = new CuponAccionInstruccion(
                    ACCION_DESCUENTO_INCLUIDAS,
                    conceptoObjetivo,
                    unidadesBonificables,
                    metadata
            );

            log.info("Beneficio X_POR_Y aplicado con descuento: cantidadPagada={}, paga={}, unidadesDescontadas={}, montoDescuento={}",
                    cantidadPagada, paga, unidadesBonificables, montoDescuento);

            return new ResultadoAplicacionBeneficio(montoDescuento, List.of(instruccion));
        }

        // 3. Si cantidadPagada == paga: el usuario solo pagó las requeridas
        // El monto de descuento es 0 y se devuelve la acción para otorgar las unidades posteriores
        CuponAccionInstruccion instruccion = new CuponAccionInstruccion(
                ACCION_BONIFICACION_POSTERIOR,
                conceptoObjetivo,
                cantidadARecibir,
                metadata
        );

        log.info("Beneficio X_POR_Y aplicado sin descuento monetario (bonificación posterior): cantidadPagada={}, paga={}, cantidadARecibir={}",
                cantidadPagada, paga, cantidadARecibir);

        return new ResultadoAplicacionBeneficio(BigDecimal.ZERO, List.of(instruccion));
    }
}
