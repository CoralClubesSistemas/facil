package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record EmailRequestDto(
        @NotEmpty List<String> destinatarios,
        @NotBlank String asunto,
        @NotBlank String cuerpo,
        List<AdjuntoDto> adjuntos
) {
}
