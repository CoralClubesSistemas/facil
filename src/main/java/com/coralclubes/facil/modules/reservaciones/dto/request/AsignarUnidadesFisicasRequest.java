package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import java.util.List;

@Builder
public record AsignarUnidadesFisicasRequest(
        @NotNull(message = "El ID del Tipo de Unidad Lógica es obligatorio")
        Integer idTipoUnidad,
        @NotEmpty(message = "Debe proporcionar al menos un ID de unidad física")
        List<Integer> idsUnidadesFisicas
) {}