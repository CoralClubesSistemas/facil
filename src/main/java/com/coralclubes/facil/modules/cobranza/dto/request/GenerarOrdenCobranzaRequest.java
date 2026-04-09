package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record GenerarOrdenCobranzaRequest(
        @NotBlank String membresia,
        @NotEmpty List<@Valid GenerarOrdenCobranzaMovimientoRequest> movimientos
) {
}

