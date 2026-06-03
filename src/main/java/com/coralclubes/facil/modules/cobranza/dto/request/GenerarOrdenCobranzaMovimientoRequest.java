package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record GenerarOrdenCobranzaMovimientoRequest(
        @NotNull Integer idMovimiento,
        @NotNull @DecimalMin(value = "0.00") BigDecimal montoCapital,
        @NotNull @DecimalMin(value = "0.00") BigDecimal montoInteres, // Cargo Total
        @NotNull @DecimalMin(value = "0.00") BigDecimal interesPago,   // NUEVO: Lo que el cliente paga
        @NotNull @DecimalMin(value = "0.00") BigDecimal interesesBonificados, // Lo que se perdona
        @NotNull @DecimalMin(value = "0.00") BigDecimal totalDescuento,
        @Size(max = 500) String justificacionDescuento,
        String usuarioAutoriza // NUEVO: Para la auditoría de la intención
) {
}
