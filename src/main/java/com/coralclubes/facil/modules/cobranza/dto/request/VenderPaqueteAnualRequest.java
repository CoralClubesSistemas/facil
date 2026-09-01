package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record VenderPaqueteAnualRequest(
        @NotBlank String membresia,
        @NotNull Integer anio,
        String mensajeAdicional
) {}
