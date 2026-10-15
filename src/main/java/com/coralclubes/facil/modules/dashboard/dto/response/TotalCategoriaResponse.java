package com.coralclubes.facil.modules.dashboard.dto.response;

import java.math.BigDecimal;

public record TotalCategoriaResponse(
    Long id,
    String categoria,
    Integer totalMembresias,
    BigDecimal total
) {}
