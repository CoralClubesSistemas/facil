package com.coralclubes.facil.modules.dashboard.dto.response;

import java.math.BigDecimal;

public record DetallesMovimientosResponse(
    Long id,
    Integer mvtId,
    String desarrollo,
    String membresia,
    String cliente,
    String movimiento,
    BigDecimal total,
    String fechaDePago,
    String numeroRecibo
) {}
