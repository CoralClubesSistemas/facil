package com.coralclubes.facil.modules.reportes.dto.request;

import java.util.List;
import java.util.Map;

public record EjecutarReporteRequest(
        Integer idTipoReporte,
        String fechaInicio,
        String fechaFin,
        String usuarioGenerador,
        Map<String, Object> parametros,
        List<String> columnas
) {}
