package com.coralclubes.facil.modules.sistema.dto.response;

import lombok.Builder;

@Builder
public record PlantillaCuerpoCorreo(
        String codigo,
        String descripcion,
        String asunto,
        String cuerpo,
        boolean activo
) {
}
