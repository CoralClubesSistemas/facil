package com.coralclubes.facil.modules.clientes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record BloqueoBeneficiarioRequest(
        @NotBlank(message = "El motivo es obligatorio")
        String motivo,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,
        LocalDate fechaFin
) {}
