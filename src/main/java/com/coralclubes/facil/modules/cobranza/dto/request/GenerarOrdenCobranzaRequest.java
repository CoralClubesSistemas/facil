package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record GenerarOrdenCobranzaRequest(
        @NotBlank String membresia,
        @NotEmpty List<@Valid GenerarOrdenCobranzaMovimientoRequest> movimientos,
        Boolean agregarIva,
        Boolean ivaIncluido
) {}

