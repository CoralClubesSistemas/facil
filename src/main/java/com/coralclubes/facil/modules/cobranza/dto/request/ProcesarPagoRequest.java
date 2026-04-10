package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record ProcesarPagoRequest(
        @NotNull String formaPagoClave,
        @NotNull @Positive BigDecimal monto,
        String metadata // Ej. folio de rastreo si es transferencia
) {}