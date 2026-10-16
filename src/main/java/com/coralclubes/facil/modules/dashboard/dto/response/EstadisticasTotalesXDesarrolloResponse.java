package com.coralclubes.facil.modules.dashboard.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record EstadisticasTotalesXDesarrolloResponse(
        Integer idDesarrollo,
        String desarrollo,
        List<EstadisticasTotalesResponse> estadisticas
) {
}
