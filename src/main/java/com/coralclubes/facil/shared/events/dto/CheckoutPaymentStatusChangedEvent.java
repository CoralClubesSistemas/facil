package com.coralclubes.facil.shared.events.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record CheckoutPaymentStatusChangedEvent(
        String eventType,
        String transactionUuid,
        String externalReference,
        String status,
        BigDecimal amount,
        String paymentMethod,
        String authorizationCode,
        LocalDateTime eventDate,
        Map<String, Object> metadata
) {}
