package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ConfiguracionUnidadesRequest(
        @NotEmpty(message = "La lista de unidades no puede estar vacía")
        List<@NotNull(message = "El ID de unidad no puede ser nulo") Integer> unidades
) {
}
