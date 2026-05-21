package com.coralclubes.facil.shared.infrastructure.integration.storage.dto;

import java.util.List;

/**
 * Agrupa las operaciones exitosas y fallidas para no romper
 * todo el lote si un archivo tiene un error (ej. permisos denegados).
 */
public record RespuestaBatchDto<T>(
        List<T> exitosos,
        List<ErrorDetalleDto> fallidos
) {
    public record ErrorDetalleDto(
            String identificador, // UUID o idCorrelacion
            String mensajeError
    ) {}
}