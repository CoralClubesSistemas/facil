package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record GenerarGestionCobranzaRequest(
        @NotBlank String membresia,
        @NotNull LocalDateTime fechaInicioVigencia,
        @NotNull LocalDateTime fechaFinVigencia,
        Boolean habilitarMeses,
        @NotEmpty List<@Valid GenerarGestionCobranzaMovimientoRequest> movimientos
) {
}

