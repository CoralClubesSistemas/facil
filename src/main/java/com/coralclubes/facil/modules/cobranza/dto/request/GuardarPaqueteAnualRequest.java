package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record GuardarPaqueteAnualRequest(
        Integer id,

        @NotNull(message = "El año es obligatorio")
        Integer anio,

        @NotNull(message = "El tipo de membresía es obligatorio")
        Integer tipoMembresia,

        @NotNull(message = "La clasificación de membresía es obligatoria")
        Integer clasificacionMembresia,

        @NotNull(message = "El desarrollo es obligatorio")
        Integer desarrollo,

        List<ConfiguracionDescuentoDto> configuracionDescuentos,

        List<ConfiguracionMovimientoDto> configuracionMovimientos
) {}
