package com.coralclubes.facil.modules.reportes.dto.response;

import lombok.Builder;

@Builder
public record ProcedimientoEjecucionDto(
        Integer tipoReporte,
        String nombreStoredProcedure,
        Integer totalParametrosEsperados
) {}
