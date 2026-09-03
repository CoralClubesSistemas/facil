package com.coralclubes.facil.modules.cobranza.model.generador_movimientos;

import com.coralclubes.facil.modules.cobranza.dto.request.CotizacionMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GeneracionMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.CotizacionMovimientoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoManualResponse;

import java.util.List;

public interface GeneracionMovimientoStrategy {
    /**
     * Determina si esta estrategia maneja el tipo de movimiento indicado.
     */
    boolean soporta(Integer tipoMovimientoId);

    /**
     * Ejecuta la lógica de generación del movimiento según la estrategia correspondiente.
     */
    List<MovimientoManualResponse> generar(GeneracionMovimientoRequest request, String usuario);

    /**
     * Proyecta y cotiza los movimientos calculados sin persistirlos en base de datos.
     */
    CotizacionMovimientoResponse cotizar(CotizacionMovimientoRequest request);
}
