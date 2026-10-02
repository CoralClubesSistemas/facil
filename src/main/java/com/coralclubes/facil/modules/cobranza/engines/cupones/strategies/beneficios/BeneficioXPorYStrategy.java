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

@Slf4j
@Component
@RequiredArgsConstructor
public class BeneficioXPorYStrategy implements CuponBeneficioStrategy {

    public static final String CLAVE = "X_POR_Y";

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
                // Soporta JSON {"paga": 2, "recibe": 2} o {"paga": 1, "obten": 2}
                if (configStr.trim().startsWith("{")) {
                    Map<String, Object> map = objectMapper.readValue(configStr, new TypeReference<>() {});
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

        // Si la configuración es 2x2 donde recibe 2 adicionales gratis, o paga X y la estancia total es (paga + gratis)
        // Ejemplo tradicional: Paga 1 y recibe 2 noches totales -> gratis = 1.
        // Si viene como {"paga": 2, "recibe": 2} interpretado como "Paga 2 y recibe 2 adicionales":
        int cantidadGratis = Math.max(1, recibe > paga ? (recibe - paga) : recibe);

        String conceptoObjetivo = beneficio.conceptoClave();

        BigDecimal montoDescuento = BigDecimal.ZERO;
        var costoUnitarioOpt = contexto.getAtributo("costoUnitario", BigDecimal.class);
        var cantidadTotalOpt = contexto.getAtributo("cantidad", Integer.class)
                .or(() -> contexto.getAtributo("noches", Integer.class));

        if (costoUnitarioOpt.isPresent() && cantidadTotalOpt.isPresent()) {
            int cantidadTotal = cantidadTotalOpt.get();
            if (cantidadTotal >= paga) {
                montoDescuento = costoUnitarioOpt.get().multiply(BigDecimal.valueOf(cantidadGratis)).setScale(2, RoundingMode.HALF_UP);
            }
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("paga", paga);
        metadata.put("recibe", recibe);
        metadata.put("cantidadGratis", cantidadGratis);
        metadata.put("conceptoId", beneficio.conceptoId());
        metadata.put("conceptoClave", beneficio.conceptoClave());
        metadata.put("beneficioId", beneficio.beneficioId());

        CuponAccionInstruccion instruccion = new CuponAccionInstruccion(
                "BONIFICACION_UNIDADES",
                conceptoObjetivo,
                cantidadGratis,
                metadata
        );

        return new ResultadoAplicacionBeneficio(montoDescuento, List.of(instruccion));
    }
}
