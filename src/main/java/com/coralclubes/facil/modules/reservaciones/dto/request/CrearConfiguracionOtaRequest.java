package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CrearConfiguracionOtaRequest(
        @NotNull(message = "El ID de la OTA es obligatorio")
        Integer idOta,

        @NotNull(message = "El ID del desarrollo es obligatorio")
        Integer idDesarrollo,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate fechaFin,

        @NotNull(message = "El porcentaje de comisión es obligatorio")
        @DecimalMin(value = "0.00", message = "El porcentaje de comisión no puede ser menor a 0")
        @DecimalMax(value = "100.00", message = "El porcentaje de comisión no puede ser mayor a 100")
        BigDecimal porcentajeComision
) {
}
