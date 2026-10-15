package com.coralclubes.facil.modules.dashboard.dto.response;

import java.math.BigDecimal;

public record InmobiliariaXCobrarDetallesResponse(
    String membresia,
    String cliente,
    String fechaVenta,
    BigDecimal intereses,
    BigDecimal costoApertura,
    BigDecimal totalVenta,
    BigDecimal totalPagado,
    BigDecimal totalDeuda
) {}
