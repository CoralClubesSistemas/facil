package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Envoltorio para enviar las características de un hotel de una sola vez.
 */
public record GuardarCaracteristicasRequest(
        @NotNull(message = "El ID es obligatorio")
        Integer id,

        @Valid
        List<RelacionCaracteristicaRequest> caracteristicas
) {}