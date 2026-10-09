package com.coralclubes.facil.modules.sistema.dto.projection;

import lombok.Builder;

/**
 * Representa la información detallada de un módulo del sistema y su relación con el módulo padre.
 * Proyección del Stored Procedure spFacilObtenerModuloPorClave.
 */
@Builder
public record ModuloDetalleProjection(
        Long id,
        String clave,
        Long padreId,
        String clavePadre,
        String nombre,
        String ruta,
        String icono,
        Integer menuFacil,
        String menuFacilDescripcion
) {
}
