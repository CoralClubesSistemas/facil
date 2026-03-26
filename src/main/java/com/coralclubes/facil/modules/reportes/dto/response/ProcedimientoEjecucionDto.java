package com.coralclubes.facil.modules.reportes.dto.response;

import lombok.Builder;

@Builder
public record ProcedimientoEjecucionDto(
        String nombreStoredProcedure,
        Integer totalParametrosEsperados
) {}
