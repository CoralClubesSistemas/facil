package com.coralclubes.facil.modules.reservaciones.dto.projection;

import lombok.Builder;

@Builder
public record ExperienciaPortalProjection(
        Integer id,
        String tag,
        String titulo,
        String descripcion,
        String link,
        String img
) {}
