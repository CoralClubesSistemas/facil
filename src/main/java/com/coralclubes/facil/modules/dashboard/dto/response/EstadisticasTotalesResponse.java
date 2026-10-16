package com.coralclubes.facil.modules.dashboard.dto.response;

import java.math.BigDecimal;

public record EstadisticasTotalesResponse(
    Integer periodo,
    String periodoNombre,
    BigDecimal total
) {}
