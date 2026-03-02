package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ConsumoOfertaRequest(
        @NotBlank String membresia,
        @NotNull Integer consecutivo,
        @NotNull Integer ofertaId
) {}