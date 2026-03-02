package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitarUrlImagenRequest(
        @NotNull(message = "El ID es obligatorio")
        Integer id,

        @NotBlank(message = "El nombre del archivo es obligatorio")
        String nombreArchivo,

        @NotBlank(message = "El Content-Type es obligatorio")
        String contentType,

        @NotNull(message = "El tamaño es obligatorio")
        @Min(value = 1, message = "El archivo no puede estar vacío")
        Long tamanoBytes
) {}