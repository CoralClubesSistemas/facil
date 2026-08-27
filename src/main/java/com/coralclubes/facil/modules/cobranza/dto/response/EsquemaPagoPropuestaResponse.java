package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record EsquemaPagoPropuestaResponse(
        Integer paqueteId,
        String value,
        String label,
        BigDecimal descuento
) {}
