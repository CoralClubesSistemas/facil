package com.coralclubes.facil.modules.cobranza.engines.cupones.dto;

import java.util.Collections;
import java.util.Map;

public record CuponAccionInstruccion(
        String tipoAccion,
        String conceptoObjetivo,
        Integer cantidad,
        Map<String, Object> metadata
) {
    public CuponAccionInstruccion {
        if (metadata == null) {
            metadata = Collections.emptyMap();
        }
    }
}
