package com.coralclubes.facil.modules.clientes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SintetizarCorreoCuponesRequest(
        @NotBlank(message = "La membresía es obligatoria")
        String membresia,
        @NotNull(message = "El año es obligatorio")
        Integer anio,
        @NotEmpty(message = "Debe proporcionar al menos un ID de cupón")
        List<Integer> ids
) {
}
