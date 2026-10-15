package com.coralclubes.facil.modules.dashboard.dto.response;

import java.math.BigDecimal;

public record MovimientoVentaXDesarrolloResponse(
    String movimiento,
    BigDecimal total
) {}
