package com.coralclubes.facil.modules.sistema.dto.projection;

import lombok.Builder;

/**
 * Representa la respuesta de la base de datos a una consulta que obtiene los módulos
 * del sistema.
 */
@Builder
public record ModuloDtoResult(
        Long id,
        Long idPadre,
        String clave,
        String nombre,
        String ruta,
        String icono,
        Integer menuFacil,
        Long orden
) {
}
