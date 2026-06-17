package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record GuardarCaracteristicaRequest(
        Integer id,

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "La descripción es obligatoria")
        String descripcion,

        @NotBlank(message = "El ícono es obligatorio")
        String icono,

        @NotNull(message = "El tipo de característica (lsv_tabla) es obligatorio")
        Integer lsvTabla
) {}
