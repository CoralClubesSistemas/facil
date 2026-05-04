package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record GenerarGestionCobranzaMovimientoRequest(
        @NotNull Integer idMovimiento,
        @NotNull @DecimalMin(value = "0.00") BigDecimal importePago,
        @NotNull @DecimalMin(value = "0.00") BigDecimal importeIntereses,
        @NotNull @DecimalMin(value = "0.00") BigDecimal importeDescuento,
        @Size(max = 255) String justificacionDescuento
) {
}

