package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Representa la solicitud de cancelación de un recibo desde el cliente.
 */
public record CancelarReciboRequest(
        @NotBlank(message = "La membresía es obligatoria.")
        String membresia,

        @NotNull(message = "El número de recibo es obligatorio.")
        Integer numeroRecibo,

        @NotNull(message = "El ID de la serie es obligatorio.")
        Integer serieReciboId,

        @NotBlank(message = "Debe proporcionar una razón para la cancelación.")
        String razonCancelacion
) {}