package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.Map;

@Builder
public record ProcesarPagoRequest(
        @NotNull String formaPagoClave,
        @NotNull @Positive BigDecimal monto,
        Map<String, Object> metadata
) {}