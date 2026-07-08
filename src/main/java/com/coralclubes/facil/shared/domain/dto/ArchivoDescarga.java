package com.coralclubes.facil.shared.domain.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ArchivoDescarga(
        String nombreArchivo,
        String urlDescarga,
        UUID uuid
) {
}
