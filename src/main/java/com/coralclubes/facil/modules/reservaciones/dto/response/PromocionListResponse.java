package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record PromocionListResponse(
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
        String urlImagen
) {}