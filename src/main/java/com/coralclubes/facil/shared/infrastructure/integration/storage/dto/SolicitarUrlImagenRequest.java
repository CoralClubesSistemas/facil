package com.coralclubes.facil.shared.infrastructure.integration.storage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitarUrlImagenRequest(
        Integer id,

        @NotBlank(message = "El nombre del archivo es obligatorio")
        String nombreArchivo,

        @NotBlank(message = "El Content-Type es obligatorio")
        String contentType,

        @NotNull(message = "El tamaño es obligatorio")
        @Min(value = 1, message = "El archivo no puede estar vacío")
        Long tamanoBytes
) {}