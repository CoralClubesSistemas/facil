package com.coralclubes.facil.modules.reportes.dto.response;

import lombok.Builder;

@Builder
public record ReporteDisponibleDto(
        Integer idReporte,
        String nombreReporte
) {}
