package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PromocionPortalDto(
        Integer id,
        String nombre,
        String descripcion,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        String codigo,
        UUID uuidImagen,
        String urlImagen
) {}
