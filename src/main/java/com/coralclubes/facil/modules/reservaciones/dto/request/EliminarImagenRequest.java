package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

/**
 * Petición individual para eliminar una imagen (solo necesitamos el UUID).
 */
@Builder
public record EliminarImagenRequest(
        @NotNull(message = "El UUID de la imagen es obligatorio")
        UUID uuid
) {}
