package com.coralclubes.facil.modules.cobranza.engines.cupones.interfaces;

import com.coralclubes.facil.modules.cobranza.dto.response.CuponCondicionResponse;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponEvaluacionContexto;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.ResultadoValidacionCondicion;

public interface CuponCondicionStrategy {

    String getClaveCondicion();

    ResultadoValidacionCondicion evaluar(CuponCondicionResponse condicion, CuponEvaluacionContexto contexto);
}
