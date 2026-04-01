package com.coralclubes.facil.modules.reservaciones.dto.projection;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record TipoUnidadCardDto(
        Integer idTipoUnidad,
        Integer idLsvTipoUnidad,
        String nombreTipoUnidad,
        Integer capacidad,
        String descripcionCorta,
        UUID uuidPortada,
        BigDecimal calificacion,
        Integer idDesarrollo,
        String nombreHotel
) {}