package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SolicitarUrlComprobanteRequest(
        @NotBlank(message = "El nombre del archivo es obligatorio.")
        String nombreArchivo,

        @NotBlank(message = "El tipo de contenido (contentType) es obligatorio.")
        String contentType,

        @NotNull(message = "El tamaño en bytes es obligatorio.")
        @Positive(message = "El tamaño en bytes debe ser mayor a 0.")
        Long tamanoBytes
) {}
