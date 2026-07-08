package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;

@Builder
public record PuntosLiberadosPdfResumenDto(
        String puntosLiberadosPeriodo,
        String puntosLiberadosPrevios,
        String totalPuntosLiberados
) {
}
