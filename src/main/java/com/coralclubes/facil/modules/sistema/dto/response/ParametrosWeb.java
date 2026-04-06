package com.coralclubes.facil.modules.sistema.dto.response;

import lombok.Builder;

@Builder
public record ParametrosWeb(
        String clave,
        String valor
) {
}
