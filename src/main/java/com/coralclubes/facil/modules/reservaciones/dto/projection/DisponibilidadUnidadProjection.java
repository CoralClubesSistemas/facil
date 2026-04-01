package com.coralclubes.facil.modules.reservaciones.dto.projection;

import java.math.BigDecimal;
import java.util.UUID;

public record DisponibilidadUnidadProjection(
        Integer idTipoUnidad,
        String nombreUnidad,
        String descripcionCorta,
        Integer capacidad,
        Integer stockDisponible,
        BigDecimal costoEstancia,
        UUID uuidImagen
) {}