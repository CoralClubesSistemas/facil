package com.coralclubes.facil.modules.cobranza.engines.cupones.interfaces;

import com.coralclubes.facil.modules.cobranza.dto.response.CuponBeneficioResponse;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponEvaluacionContexto;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.ResultadoAplicacionBeneficio;

public interface CuponBeneficioStrategy {

    String getClaveBeneficio();

    ResultadoAplicacionBeneficio aplicar(CuponBeneficioResponse beneficio, CuponEvaluacionContexto contexto);
}
