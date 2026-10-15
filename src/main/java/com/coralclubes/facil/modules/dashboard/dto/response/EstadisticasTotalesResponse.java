package com.coralclubes.facil.modules.dashboard.dto.response;

import java.math.BigDecimal;

public record EstadisticasTotalesResponse(
    Integer mes,
    String mesNombre,
    BigDecimal total
) {}
