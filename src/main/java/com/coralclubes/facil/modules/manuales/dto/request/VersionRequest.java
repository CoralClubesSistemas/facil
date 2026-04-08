package com.coralclubes.facil.modules.manuales.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record VersionRequest(
        @NotNull(message = "El ID del manual es obligatorio")
        Integer manualId,

        String cambios,

        @NotNull(message = "El UUID del archivo es obligatorio")
        String uuid,

        @NotBlank(message = "El nombre del archivo es obligatorio")
        String nombreArchivo,

        @NotBlank(message = "El tipo de archivo es obligatorio")
        String tipo
) {}