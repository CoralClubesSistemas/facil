package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

@Builder
public record MapeoPeriodicidadResponse(
        Integer periodicidadId,
        String periodoUnidad,
        Integer cantidadXPeriodo
) {}
