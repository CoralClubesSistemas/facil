package com.coralclubes.facil.modules.dashboard.dto.response;

import java.math.BigDecimal;

public record InmobiliariaXCobrarTotalesResponse(
    Long id,
    String clasificacion,
    BigDecimal totalVenta,
    BigDecimal totalPagado,
    BigDecimal totalDeuda,
    Integer totalMembresias
) {}
