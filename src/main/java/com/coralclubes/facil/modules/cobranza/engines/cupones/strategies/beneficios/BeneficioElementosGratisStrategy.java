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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeneficioElementosGratisStrategy implements CuponBeneficioStrategy {

    public static final String CLAVE = "ELEMENTOS_GRATIS";

    private final ObjectMapper objectMapper;

    @Override
    public String getClaveBeneficio() {
        return CLAVE;
    }

    @Override
    public ResultadoAplicacionBeneficio aplicar(CuponBeneficioResponse beneficio, CuponEvaluacionContexto contexto) {
        String configStr = beneficio.configuracionBeneficio();
        int cantidad = 1;

        if (configStr != null && !configStr.isBlank()) {
            try {
                // Soporta JSON {"cantidad": 1} o entero directo "1"
                if (configStr.trim().startsWith("{")) {
                    Map<String, Object> map = objectMapper.readValue(configStr, new TypeReference<>() {});
                    Object cantObj = map.get("cantidad");
                    if (cantObj instanceof Number n) {
                        cantidad = n.intValue();
                    } else if (cantObj instanceof String s) {
                        cantidad = Integer.parseInt(s.trim());
                    }
                } else {
                    cantidad = Integer.parseInt(configStr.trim());
                }
            } catch (Exception e) {
                log.warn("Error al parsear configuracion de elementos_gratis: '{}', usando cantidad=1", configStr);
            }
        }

        String conceptoObjetivo = beneficio.conceptoClave();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("cantidad", cantidad);
        metadata.put("conceptoId", beneficio.conceptoId());
        metadata.put("conceptoDescripcion", beneficio.concepto());
        metadata.put("beneficioId", beneficio.beneficioId());

        CuponAccionInstruccion instruccion = new CuponAccionInstruccion(
                "ENTREGA_ELEMENTOS_GRATIS",
                conceptoObjetivo,
                cantidad,
                metadata
        );

        return new ResultadoAplicacionBeneficio(BigDecimal.ZERO, List.of(instruccion));
    }
}
