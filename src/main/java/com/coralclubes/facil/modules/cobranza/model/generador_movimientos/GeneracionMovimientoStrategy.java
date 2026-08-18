package com.coralclubes.facil.modules.cobranza.model.generador_movimientos;

import com.coralclubes.facil.modules.cobranza.dto.request.GeneracionMovimientoRequest;

public interface GeneracionMovimientoStrategy {
    /**
     * Determina si esta estrategia maneja el tipo de movimiento indicado.
     */
    boolean soporta(Integer tipoMovimientoId);

    /**
     * Ejecuta la lógica de generación del movimiento según la estrategia correspondiente.
     */
    void generar(GeneracionMovimientoRequest request, String usuario);
}
