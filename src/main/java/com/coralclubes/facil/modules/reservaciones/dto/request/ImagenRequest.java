package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

/**
 * Petición individual de imagen para guardar (con su orden y si es portada).
 */
@Builder
public record ImagenHotelRequest(
        @NotNull(message = "El UUID de la imagen es obligatorio")
        UUID uuid,

        boolean esPortada,
        int orden
) {}