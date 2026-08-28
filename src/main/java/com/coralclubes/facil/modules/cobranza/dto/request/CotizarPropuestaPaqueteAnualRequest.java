package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record CotizarPropuestaPaqueteAnualRequest(
        @NotBlank(message = "La membresía es obligatoria")
        String membresia,

        @NotNull(message = "El año es obligatorio")
        Integer anio,

        List<String> esquemas,

        List<CotizarPropuestaMovimientoParamDto> movimientos
) {}
