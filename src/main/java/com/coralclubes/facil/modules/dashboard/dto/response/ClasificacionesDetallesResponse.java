package com.coralclubes.facil.modules.dashboard.dto.response;

import java.math.BigDecimal;

public record ClasificacionesDetallesResponse(
    Integer id,
    String desarrollo,
    String membresia,
    String movimiento,
    String fechaPago,
    BigDecimal cargo,
    BigDecimal pendiente,
    BigDecimal descuento,
    BigDecimal bonificado,
    BigDecimal intereses,
    BigDecimal interesesBonificados,
    BigDecimal interesesPagados,
    BigDecimal neto,
    String recibo
) {}
