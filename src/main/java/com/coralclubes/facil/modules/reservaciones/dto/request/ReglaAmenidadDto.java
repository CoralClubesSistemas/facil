package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReglaAmenidadDto(
        @NotNull(message = "El ID del artículo es obligatorio")
        Integer idArticulo,

        @NotNull(message = "La cantidad base es obligatoria")
        @Min(value = 0, message = "La cantidad base no puede ser negativa")
        Integer cantidadBase,

        @NotNull(message = "La cantidad por persona es obligatoria")
        @Min(value = 0, message = "La cantidad por persona no puede ser negativa")
        Integer cantidadPorPersona
) {}