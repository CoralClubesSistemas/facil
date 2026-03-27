package com.coralclubes.facil.modules.reportes.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record HistorialReporteDto(
        Integer idBitacora,
        Integer idTipoReporte,
        String nombreReporte,
        String nombreArchivo,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaCompletado,
        String estatus,
        UUID uuidArchivo,
        Integer tiempoEjecucionMs,
        String parametrosJson,
        String mensajeError
) {}