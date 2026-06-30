package com.coralclubes.facil.shared.infrastructure.integration.checkout.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record CheckoutPaymentEventDto(
        String eventType,
        String transactionUuid,
        String externalReference,
        String status,
        BigDecimal amount,
        String paymentMethod,
        String authorizationCode,
        LocalDateTime eventDate,
        List<Integer> items,
        Map<String, Object> metadata
) {}
