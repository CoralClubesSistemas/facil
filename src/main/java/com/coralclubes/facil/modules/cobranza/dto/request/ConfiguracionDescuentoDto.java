package com.coralclubes.facil.modules.cobranza.dto.request;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ConfiguracionDescuentoDto(
        String esquema,
        BigDecimal descuento
) {}
