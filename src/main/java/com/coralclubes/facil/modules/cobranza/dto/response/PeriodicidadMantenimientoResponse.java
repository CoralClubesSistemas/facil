package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

@Builder
public record PeriodicidadMantenimientoResponse(
        Integer periodicidadId,
        String periodicidad,
        Integer cantidadPorPeriodo
) {}
