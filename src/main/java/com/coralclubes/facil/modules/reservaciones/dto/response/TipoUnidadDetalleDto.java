package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TipoUnidadDetalleDto(
        Integer idTipoUnidad,
        Integer idDesarrollo,
        String nombreDesarrollo,
        Integer idLsvTipoUnidad,
        String nombreTipoUnidad,
        Integer capacidad,
        String descripcionCorta,
        String descripcionLarga,
        BigDecimal calificacion
) {}
