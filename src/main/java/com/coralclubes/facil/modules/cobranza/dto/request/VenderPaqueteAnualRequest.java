package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record VenderPaqueteAnualRequest(
        @NotNull Integer propuestaId,
        @NotBlank String membresia,
        @NotNull Integer procedenciaId,
        @NotBlank String procedencia,
        Boolean generarOrdenLinkPago,
        String correoEnvioLinkPago,
        Integer anio,
        String mensajeAdicional
) {}

