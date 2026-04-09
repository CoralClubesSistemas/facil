package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EstadoCuentaAdeudoRequest(
        @NotBlank String membresia,
        LocalDateTime fechaCorte
) {
}

