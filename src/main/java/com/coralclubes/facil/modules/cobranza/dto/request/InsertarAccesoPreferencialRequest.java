package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InsertarAccesoPreferencialRequest(
        @NotBlank(message = "La membresía es obligatoria")
        String membresia,

        @NotNull(message = "El número de beneficiario es obligatorio")
        Integer numBeneficiario,

        @NotNull(message = "El motivo es obligatorio")
        Integer motivo,

        String notaRecomendaciones,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDateTime fechaInicio,
        LocalDateTime fechaFinal
) {}
