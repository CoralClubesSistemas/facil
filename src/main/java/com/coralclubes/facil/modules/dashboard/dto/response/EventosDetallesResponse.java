package com.coralclubes.facil.modules.dashboard.dto.response;

import java.math.BigDecimal;

public record EventosDetallesResponse(
    Long id,
    String membresia,
    String cliente,
    BigDecimal total,
    String fechaPago,
    String fechaEvento,
    String tipoEvento,
    String recibo
) {}
