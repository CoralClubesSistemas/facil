package com.coralclubes.facil.modules.cobranza.engines.cupones.engine;

import com.coralclubes.facil.modules.cobranza.dto.response.CuponBeneficioResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponCondicionResponse;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponAccionInstruccion;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponEvaluacionContexto;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponLiquidacionResult;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.ResultadoAplicacionBeneficio;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.ResultadoValidacionCondicion;
import com.coralclubes.facil.modules.cobranza.engines.cupones.interfaces.CuponBeneficioStrategy;
import com.coralclubes.facil.modules.cobranza.engines.cupones.interfaces.CuponCondicionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/***
 * Motor principal para la gestión de cupones.
 * Maneja la validación de condiciones y la aplicación de beneficios a traves de estrategias registradas.
 */
@Slf4j
@Service
public class CuponesEngine {

    private final Map<String, CuponCondicionStrategy> mapaCondiciones;
    private final Map<String, CuponBeneficioStrategy> mapaBeneficios;

    public CuponesEngine(
            List<CuponCondicionStrategy> condiciones,
            List<CuponBeneficioStrategy> beneficios
    ) {
        this.mapaCondiciones = condiciones.stream()
                .collect(Collectors.toMap(
                        strategy -> strategy.getClaveCondicion().toUpperCase(),
                        Function.identity()
                ));

        this.mapaBeneficios = beneficios.stream()
                .collect(Collectors.toMap(
                        strategy -> strategy.getClaveBeneficio().toUpperCase(),
                        Function.identity()
                ));

        log.info("CuponesEngine inicializado con {} condiciones y {} beneficios registrados.",
                mapaCondiciones.size(), mapaBeneficios.size());
    }

    /**
     * Metodo encargado de unicamente validar si un cupon cumple con todas las condiciones que contiene
     * a partir de su contexto.
     *
     * @param condiciones Lista de condiciones a evaluar.
     * @param contexto    Contexto de evaluación que contiene los atributos necesarios para la validación.
     * @return ResultadoValidacionCondicion que indica si el cupon es válido o no, junto con un mensaje de rechazo si no es válido.
     *
     */
    public ResultadoValidacionCondicion validarCondiciones(
            List<CuponCondicionResponse> condiciones,
            CuponEvaluacionContexto contexto
    ) {
        // si no hay condiciones a evaluar por defecto es válido
        if (condiciones == null || condiciones.isEmpty()) {
            return ResultadoValidacionCondicion.valida();
        }

        for (CuponCondicionResponse condicion : condiciones) {
            if (condicion.claveCondicion() == null || condicion.claveCondicion().isBlank()) {
                continue;
            }

            String clave = condicion.claveCondicion().toUpperCase();
            CuponCondicionStrategy estrategia = mapaCondiciones.get(clave);

            // evaluamos si la solicitus pide una estrategia que no está registrada o soportada
            if (estrategia == null) {
                log.warn("Estrategia de condición no soportada o no registrada: {}", clave);
                return ResultadoValidacionCondicion.invalida(
                        "La condición requerida '" + condicion.claveCondicion() + "' no está soportada actualmente."
                );
            }

            ResultadoValidacionCondicion resultado = estrategia.evaluar(condicion, contexto); // nos devuelve el resultado de la validacion

            // si al menos una condición no es válida, el cupón es rechazado y se retorna el mensaje de rechazo correspondiente
            if (!resultado.esValida()) {
                log.info("Cupón rechazado por condición [{}]: {}", clave, resultado.mensajeRechazo());
                return resultado;
            }
        }

        return ResultadoValidacionCondicion.valida();
    }

    /**
     * Liquidar un cupón, aplicando las condiciones y beneficios correspondientes.
     *
     * @param condiciones Las condiciones a evaluar.
     * @param beneficios  Los beneficios a aplicar.
     * @param contexto    El contexto de evaluación.
     * @return El resultado de la liquidación del cupón.
     */
    public CuponLiquidacionResult liquidar(
            List<CuponCondicionResponse> condiciones,
            List<CuponBeneficioResponse> beneficios,
            CuponEvaluacionContexto contexto
    ) {
        BigDecimal montoOriginal = contexto.montoOriginal() != null ? contexto.montoOriginal() : BigDecimal.ZERO;

        // Evaluamos si cumple con todas las condiciones, si alguna falla se rechaza el cupón y se devuelve el monto original sin descuento
        ResultadoValidacionCondicion validacion = validarCondiciones(condiciones, contexto);
        if (!validacion.esValida()) {
            return CuponLiquidacionResult.rechazado(montoOriginal, validacion.mensajeRechazo());
        }

        // acumulamos los descuentos devueltos por cada beneficio valido
        BigDecimal descuentoAcumulado = BigDecimal.ZERO;
        List<CuponAccionInstruccion> instruccionesAcciones = new ArrayList<>();

        if (beneficios != null) {
            for (CuponBeneficioResponse beneficio : beneficios) {
                if (beneficio.claveBeneficio() == null || beneficio.claveBeneficio().isBlank()) {
                    continue;
                }

                String clave = beneficio.claveBeneficio().toUpperCase();
                CuponBeneficioStrategy estrategia = mapaBeneficios.get(clave);

                if (estrategia == null) {
                    log.warn("Estrategia de beneficio no soportada: {}", clave);
                    continue;
                }

                ResultadoAplicacionBeneficio resultadoBeneficio = estrategia.aplicar(beneficio, contexto);
                if (resultadoBeneficio != null) {
                    if (resultadoBeneficio.montoDescuento() != null) {
                        descuentoAcumulado = descuentoAcumulado.add(resultadoBeneficio.montoDescuento());
                    }
                    if (resultadoBeneficio.accionesInstrucciones() != null && !resultadoBeneficio.accionesInstrucciones().isEmpty()) {
                        instruccionesAcciones.addAll(resultadoBeneficio.accionesInstrucciones());
                    }
                }
            }
        }

        // El descuento acumulado no debe sobrepasar el monto original
        BigDecimal descuentoFinal = descuentoAcumulado.min(montoOriginal);

        log.info("Liquidación de cupón: Monto original = {}, Descuento acumulado = {}, Descuento final aplicado = {}", montoOriginal, descuentoAcumulado, descuentoFinal);
        return CuponLiquidacionResult.aprobado(montoOriginal, descuentoFinal, instruccionesAcciones);
    }
}
