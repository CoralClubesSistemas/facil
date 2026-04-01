package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record TemporadaMasivaWrapperRequest(
        @NotEmpty(message = "La lista de temporadas no puede estar vacía")
        @Valid
        List<TemporadaMasivaRequest> temporadas
) {}