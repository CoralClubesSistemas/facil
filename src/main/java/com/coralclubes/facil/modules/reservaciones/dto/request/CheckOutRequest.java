package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckOutRequest(
        @NotBlank String membresia,
        @NotNull Integer consecutivo
) {}