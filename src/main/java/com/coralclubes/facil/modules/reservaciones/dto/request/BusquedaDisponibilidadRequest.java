package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record BusquedaDisponibilidadRequest(
        @NotNull(message = "El destino es obligatorio") Integer destinoId,
        @NotNull(message = "La fecha de entrada es obligatoria") LocalDate fechaEntrada,
        @NotNull(message = "La fecha de salida es obligatoria") LocalDate fechaSalida,
        @NotNull(message = "La cantidad de personas es obligatoria") @Min(1) Integer personas,
        String membresia // Es opcional
) {}