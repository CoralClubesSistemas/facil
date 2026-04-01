package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record TarifasWrapperRequest(
        @NotEmpty(message = "Debe enviar al menos una tarifa a guardar")
        @Valid List<TarifaRequest> tarifas
) {}