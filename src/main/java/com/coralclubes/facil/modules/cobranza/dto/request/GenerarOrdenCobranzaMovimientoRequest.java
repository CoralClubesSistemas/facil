package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record GenerarOrdenCobranzaMovimientoRequest(
        @NotNull Integer idMovimiento,
        @NotNull @DecimalMin(value = "0.00") BigDecimal montoCapital,
        @NotNull @DecimalMin(value = "0.00") BigDecimal montoInteres,
        @NotNull @DecimalMin(value = "0.00") BigDecimal interesesBonificados,
        @NotNull @DecimalMin(value = "0.00") BigDecimal totalDescuento,
        @NotNull @DecimalMin(value = "0.00") BigDecimal montoIva,
        @Size(max = 500) String justificacionDescuento
) {
}

