package com.coralclubes.facil.modules.reservaciones.dto.request;

import lombok.Builder;
import java.time.LocalDate;

@Builder
public record FiltroConsultaGeneral(
        Integer desarrolloId,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String tipoFecha,      // ENTRADA, SALIDA, REGISTRO
        String estatusClave,
        String busqueda,
        Integer pageNumber,    // Por defecto 1
        Integer pageSize       // Por defecto 50
) {
    // Valores por defecto seguros
    public FiltroConsultaGeneral {
        if (tipoFecha == null || tipoFecha.isBlank()) tipoFecha = "ENTRADA";
        if (pageNumber == null || pageNumber < 1) pageNumber = 1;
        if (pageSize == null || pageSize < 1) pageSize = 50;
    }
}