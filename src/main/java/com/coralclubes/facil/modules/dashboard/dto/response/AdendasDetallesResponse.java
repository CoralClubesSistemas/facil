package com.coralclubes.facil.modules.dashboard.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdendasDetallesResponse(
    String id,
    String membresia,
    LocalDateTime fechaVenta,
    LocalDateTime fechaMovimiento,
    BigDecimal importeCargo,
    BigDecimal importePendiente,
    BigDecimal totalBonificaciones,
    BigDecimal totalPagos,
    String ultimoRecibo,
    String usuario,
    String estatusMovimiento,
    String tipoMovimiento,
    String descripcionMovimiento
) {}
