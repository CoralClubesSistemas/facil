package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GuardarFormatoImagenCuponRequest(
        Integer id,
        @NotNull(message = "El cuponId es obligatorio")
        Integer cuponId,
        UUID uuid,
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,
        @NotBlank(message = "La configuración es obligatoria")
        String configuracion,
        @NotBlank(message = "Los metadatos son obligatorios")
        String metadata
) {
}
