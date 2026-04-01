package com.coralclubes.facil.modules.reservaciones.dto.projection;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PromocionListProjection (
    Integer idPromocion,
    String nombrePromocion,
    String descripcionPromocion,
    String codigoPromocion,
    Integer stockTotal,
    Integer stockDisponible,
    LocalDateTime fechaInicio,
    LocalDateTime fechaFin,
    Boolean esPrivada,
    Boolean esGlobal,
    LocalDateTime fechaVisible,
    UUID uuidImagen
) {}
