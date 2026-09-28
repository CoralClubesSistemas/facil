package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PagarAdeudosReservacionRequest(
        @NotBlank(message = "La membresía es obligatoria.")
        String membresia,

        @NotNull(message = "El folio de la reservación es obligatorio.")
        Integer folio,

        @NotEmpty(message = "Debe proporcionar al menos un movimiento para pagar.")
        List<Integer> idMovimientos
) {}
