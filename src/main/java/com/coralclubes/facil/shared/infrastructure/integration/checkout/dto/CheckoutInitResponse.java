package com.coralclubes.facil.shared.infrastructure.integration.checkout.dto;

public record CheckoutInitResponse(
        String uuid,
        String checkoutUrl,
        String expirationDate
) {}
