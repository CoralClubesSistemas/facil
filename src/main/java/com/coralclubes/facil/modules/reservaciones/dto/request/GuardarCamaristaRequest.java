package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GuardarCamaristaRequest(
        @NotNull(message = "El ID es obligatorio (0 para nuevo)")
        Integer idCamarista,

        @NotBlank(message = "El nombre de la camarista es obligatorio")
        String nombre
) {}