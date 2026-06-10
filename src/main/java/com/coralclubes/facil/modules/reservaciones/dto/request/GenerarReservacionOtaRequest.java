package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record GenerarReservacionOtaRequest(
        @NotNull(message = "El ID de la OTA es obligatorio")
        Integer idOta,

        @NotNull(message = "El ID del desarrollo es obligatorio")
        Integer idDesarrollo,

        String codigoVoucherOta,

        BigDecimal montoTarifaOta,

        String rsvMembresia,

        @NotBlank(message = "El nombre de la reservación es obligatorio")
        String nombreReservacion,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El formato del correo electrónico es inválido")
        String correoElectronico,

        String telefono,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate fechaFin,

        @NotNull(message = "El tipo de unidad es obligatorio")
        Integer tipoUnidad,

        @NotNull(message = "El ID de la unidad es obligatorio")
        Integer idUnidad,

        Integer numeroSocios,

        String peticionEspecial
) {
}
