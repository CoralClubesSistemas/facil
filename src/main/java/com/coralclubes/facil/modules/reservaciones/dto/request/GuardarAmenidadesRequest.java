package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record GuardarAmenidadesRequest(
        @NotNull(message = "El ID del tipo de unidad es obligatorio")
        Integer rhdtId,

        @NotNull(message = "La lista de reglas no puede ser nula")
        @Valid
        List<ReglaAmenidadDto> reglas
) {}