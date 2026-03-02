package com.coralclubes.facil.modules.reservaciones.dto.projection;

import lombok.Builder;

/**
 * Proyección del SP: spResvObtenerCaracteristicasXHotel
 * Devuelve las amenidades cruzadas con su catálogo base.
 */
@Builder
public record CaracteristicaDto(
        Integer idCaracteristica,
        String nombre,
        String descripcion,
        String icono,
        Integer cantidad,
        Integer idTipo
) {}
