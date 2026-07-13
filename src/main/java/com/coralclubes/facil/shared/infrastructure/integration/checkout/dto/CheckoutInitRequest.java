package com.coralclubes.facil.shared.infrastructure.integration.checkout.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record CheckoutInitRequest(
        String externalReference,
        BigDecimal amount,
        String currency,
        String description,
        String payerEmail,
        Integer desarrollo,
        Boolean isSandbox,
        List<CheckoutItem> items,
        String urlRedirectOnSuccess,
        String urlRedirectOnFailure,
        String urlRedirectOnCancel,
        String expirationDate
) {
    @Builder
    public record CheckoutItem(
            Integer id,
            String title,
            Integer quantity,
            BigDecimal unitPrice,
            String description
    ) {}
}
