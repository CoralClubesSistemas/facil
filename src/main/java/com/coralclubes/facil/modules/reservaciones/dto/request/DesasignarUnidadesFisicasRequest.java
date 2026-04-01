package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import java.util.List;

@Builder
public record DesasignarUnidadesFisicasRequest(
        @NotEmpty(message = "Debe proporcionar al menos un ID de unidad física")
        List<Integer> idsUnidadesFisicas
) {}