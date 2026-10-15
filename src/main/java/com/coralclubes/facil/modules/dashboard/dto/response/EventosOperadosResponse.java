package com.coralclubes.facil.modules.dashboard.dto.response;

import java.math.BigDecimal;

public record EventosOperadosResponse(
    Long id,
    String membresia,
    String desarrollo,
    String cliente,
    String fechaEvento,
    String tipoEvento,
    String totalPersonasEvento,
    BigDecimal importeTotalEvento
) {}
