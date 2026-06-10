package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

@Builder
public record UnidadOtaResponse(
        Integer idUnidad,
        String nombreUnidad,
        String tipoUnidad,
        Integer capacidadUnidad
) {
}
