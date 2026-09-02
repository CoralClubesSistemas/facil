package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TarifaMovimientoResponse(
        Integer anio,
        BigDecimal cuota
) {}
