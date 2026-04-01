package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemCarritoRequest(
        @NotNull(message = "El tipo de unidad es obligatorio") Integer idTipoUnidad,
        @NotNull(message = "La cantidad es obligatoria") @Min(1) Integer cantidad
) {}