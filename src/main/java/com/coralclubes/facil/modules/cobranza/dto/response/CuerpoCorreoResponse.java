package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

@Builder
public record CuerpoCorreoResponse(
        String asunto,
        String cuerpo
) {
}
