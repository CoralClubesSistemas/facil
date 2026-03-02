package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Envoltorio para cambiar la portada rápidamente.
 */
public record CambiarPortadaRequest(
        @NotNull(message = "El ID es obligatorio")
        Integer id,

        @NotNull(message = "El UUID de la nueva portada es obligatorio")
        UUID nuevaPortadaUuid
) {}