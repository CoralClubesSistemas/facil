package com.coralclubes.facil.shared.platform.templating.dto;

import lombok.Builder;

@Builder
public record PlantillaCuerpoCorreo(
        Integer id,
        String codigo,
        String nombre,
        String descripcion,
        String asunto,
        String cuerpo,
        Boolean activo
) {
}
