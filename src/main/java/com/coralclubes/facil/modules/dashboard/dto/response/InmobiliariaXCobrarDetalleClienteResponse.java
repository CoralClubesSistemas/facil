package com.coralclubes.facil.modules.dashboard.dto.response;

import java.math.BigDecimal;

public record InmobiliariaXCobrarDetalleClienteResponse(
    Long id,
    String membresia,
    String descripcion,
    String fechaVencimiento,
    BigDecimal importeCargo,
    BigDecimal importeAbono,
    BigDecimal importePendiente
) {}
