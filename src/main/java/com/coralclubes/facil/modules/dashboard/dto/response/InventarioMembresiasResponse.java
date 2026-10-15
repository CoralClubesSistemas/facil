package com.coralclubes.facil.modules.dashboard.dto.response;

import java.math.BigDecimal;

public record InventarioMembresiasResponse(
    Long id,
    String desarrollo,
    String cartera,
    String membresia,
    String cliente,
    String tipoMembresia,
    BigDecimal totalAdeudo
) {}
