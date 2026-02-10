package com.coralclubes.facil.modules.sistema.dto.projection;

import lombok.Builder;

/**
 * Representa La respuesta de la base de datos a una consulta que obtiene los modulos
 * del sistema.
 * Aun que tambien se puede usar para mapear la solicitudes que
 * vengan del cliente para crear un nuevo modulo o eliminar un nuevo modulo.
 */
@Builder
public record ModuloDtoResult(
        Integer id,
        Integer idPadre,
        String clave,
        String nombre,
        String ruta,
        String icono,
        Integer menuFacil
) {
}
