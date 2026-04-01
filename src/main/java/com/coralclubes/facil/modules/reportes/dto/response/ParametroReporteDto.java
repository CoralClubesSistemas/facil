package com.coralclubes.facil.modules.reportes.dto.response;

import lombok.Builder;

@Builder
public record ParametroReporteDto(
        Integer idParametro,
        String nombreFiltroUI,
        String endpointData
) {}
