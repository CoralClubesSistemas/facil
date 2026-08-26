package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record BajaAccesoPreferencialRequest(
        @NotBlank(message = "La membresía es obligatoria")
        String membresia,

        @NotNull(message = "El número de beneficiario es obligatorio")
        Integer numBeneficiario
) {}
