package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotNull;

public record ReactivarUnidadRequest(
        @NotNull(message = "El ID de la unidad física es obligatorio")
        Integer idUnidadFisica
) {}