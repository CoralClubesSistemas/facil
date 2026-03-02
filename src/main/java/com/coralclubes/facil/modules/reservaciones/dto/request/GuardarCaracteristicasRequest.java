package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Envoltorio para enviar las características de un hotel de una sola vez.
 */
public record GuardarCaracteristicasHotelRequest(
        @NotNull(message = "El ID del hotel es obligatorio")
        Integer idHotel,

        @Valid
        List<RelacionCaracteristicaRequest> caracteristicas
) {}