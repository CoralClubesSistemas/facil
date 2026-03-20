package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record GenerarCargoRequest(
        @NotBlank String membresia,
        @NotNull Integer consecutivo,
        @NotNull Integer tipoMovimiento,
        BigDecimal importe, // Es opcional, si viene nulo el SP tomará el del catálogo
        String referencia,
        String observaciones
) {}