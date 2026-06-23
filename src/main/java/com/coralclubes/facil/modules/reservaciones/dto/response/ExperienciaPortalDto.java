package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

/**
 * Representa la información de una experiencia del portal de reservaciones.
 */
@Builder
public record ExperienciaPortalDto(
        Integer id,
        String tag,
        String titulo,
        String descripcion,
        String link,
        String img
) {}
