package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record CrearReservaTemporalRequest(
        @NotEmpty(message = "El carrito no puede estar vacío")
        @Valid List<ItemCarritoRequest> carrito,

        @NotNull(message = "La fecha de entrada es obligatoria") LocalDate fechaEntrada,
        @NotNull(message = "La fecha de salida es obligatoria") LocalDate fechaSalida,

        String membresia // Nullable si es público general
) {}