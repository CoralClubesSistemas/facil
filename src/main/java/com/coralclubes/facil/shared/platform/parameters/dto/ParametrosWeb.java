package com.coralclubes.facil.shared.platform.parameters.dto;

import lombok.Builder;

@Builder
public record ParametrosWeb(
        String clave,
        String valor
) {
}
