package com.coralclubes.facil.modules.prospectos.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO para registrar una cita de un prospecto.
 */
@Builder
public record ProspectoRegistrarCitaRequest(
        @NotNull(message = "El ID del prospecto es obligatorio")
        Integer prospectoId,

        @NotNull(message = "La fecha de inicio de la cita es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "La hora de inicio de la cita es obligatoria")
        LocalTime horaInicio,

        @NotNull(message = "El ID del lugar de la cita es obligatorio")
        Integer lugarCita,

        String nota
) {
}
