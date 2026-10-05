package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record GenerarOrdenCobranzaMovimientoRequest(
        @NotNull Integer idMovimiento,
        @NotNull @DecimalMin(value = "0.00") BigDecimal montoCapital,
        @NotNull @DecimalMin(value = "0.00") BigDecimal montoInteres,
        @NotNull @DecimalMin(value = "0.00") BigDecimal interesPago,
        @NotNull @DecimalMin(value = "0.00") BigDecimal interesesBonificados,
        @NotNull @DecimalMin(value = "0.00") BigDecimal totalDescuento,
        @Size(max = 500) String justificacionDescuento,
        String usuarioAutoriza,
        List<BigDecimal> porcentajesDescuentoCascada
) {
}
