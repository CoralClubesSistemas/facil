package com.coralclubes.facil.shared.infrastructure.domain.dto;

import lombok.Builder;

@Builder
public record ArchivoDescarga(
        String nombreArchivo,
        String urlDescarga
) {
}
