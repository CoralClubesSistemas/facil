package com.coralclubes.facil.modules.cobranza.dto.response;

public record CuponesEstadisticasKpiResponse(
        Integer totalEmitidos,
        Integer totalCanjeados,
        Integer totalDisponibles
) {}
