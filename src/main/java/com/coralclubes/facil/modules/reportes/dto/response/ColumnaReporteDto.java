package com.coralclubes.facil.modules.reportes.dto.response;

import lombok.Builder;

@Builder
public record ColumnaReporteDto(
        String nombreColumna,
        Integer orden,
        Boolean visible
) {}
