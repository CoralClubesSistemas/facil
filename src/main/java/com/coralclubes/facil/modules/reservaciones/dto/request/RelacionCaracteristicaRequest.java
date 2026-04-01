package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * Petición individual para relacionar una característica (Ej: dentro de un array JSON).
 */
@Builder
public record RelacionCaracteristicaRequest(
        @NotNull(message = "El ID de la característica es obligatorio")
        Integer idCaracteristica,

        @NotNull(message = "La cantidad es obligatoria")
        Integer cantidad
) {}