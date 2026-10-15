package com.coralclubes.facil.modules.dashboard.dto.response;

import java.math.BigDecimal;

public record ClasificacionesResponse(
    String movimiento,
    String membresias,
    BigDecimal cargo,
    BigDecimal pendiente,
    BigDecimal descuento,
    BigDecimal bonificado,
    BigDecimal intereses,
    BigDecimal interesesBonificados,
    BigDecimal interesesPagados,
    BigDecimal neto
) {}
