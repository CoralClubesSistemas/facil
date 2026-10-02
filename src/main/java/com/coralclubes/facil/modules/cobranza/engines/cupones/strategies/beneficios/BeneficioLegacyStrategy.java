package com.coralclubes.facil.modules.cobranza.engines.cupones.strategies.beneficios;

import com.coralclubes.facil.modules.cobranza.dto.response.CuponBeneficioResponse;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponAccionInstruccion;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponEvaluacionContexto;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.ResultadoAplicacionBeneficio;
import com.coralclubes.facil.modules.cobranza.engines.cupones.interfaces.CuponBeneficioStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BeneficioLegacyStrategy implements CuponBeneficioStrategy {

    public static final String CLAVE = "LEGACY";

    @Override
    public String getClaveBeneficio() {
        return CLAVE;
    }

    @Override
    public ResultadoAplicacionBeneficio aplicar(CuponBeneficioResponse beneficio, CuponEvaluacionContexto contexto) {
        log.info("Procesando beneficio de tipo LEGACY para cupón: beneficioId={}, concepto={}",
                beneficio.beneficioId(), beneficio.concepto());

        String conceptoObjetivo = beneficio.conceptoClave();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("configuracion", beneficio.configuracionBeneficio());
        metadata.put("conceptoId", beneficio.conceptoId());
        metadata.put("conceptoClave", beneficio.conceptoClave());
        metadata.put("beneficioId", beneficio.beneficioId());

        CuponAccionInstruccion instruccion = new CuponAccionInstruccion(
                "APLICAR_BENEFICIO_LEGACY",
                conceptoObjetivo,
                1,
                metadata
        );

        return new ResultadoAplicacionBeneficio(BigDecimal.ZERO, List.of(instruccion));
    }
}
