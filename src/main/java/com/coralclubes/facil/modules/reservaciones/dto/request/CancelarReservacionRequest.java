package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CancelarReservacionRequest(
        @NotBlank(message = "La membresía es obligatoria")
        String membresia,

        @NotNull(message = "El consecutivo es obligatorio")
        Integer consecutivo,

        @NotBlank(message = "El motivo de la cancelación es obligatorio")
        @Size(max = 250, message = "El motivo no puede exceder los 250 caracteres")
        String motivoCancelacion,

        @NotNull(message = "Debe indicar si se cobra la cuota de cancelación")
        Boolean cobrarCuotaCancelacion
) {}