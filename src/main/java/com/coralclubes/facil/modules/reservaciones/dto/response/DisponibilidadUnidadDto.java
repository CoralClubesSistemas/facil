package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record DisponibilidadUnidadDto(
        Integer idTipoUnidad,
        String nombreUnidad,
        String descripcionCorta,
        Integer capacidad,
        Integer stockDisponible,
        BigDecimal costoEstancia,
        String urlImagen
) {}