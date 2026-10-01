package com.coralclubes.facil.modules.clientes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConsumirCuponMembresiaRequest(
        @NotBlank(message = "La membresía es obligatoria")
        String membresia,

        @NotNull(message = "El identificador del paquete (PqacId) es obligatorio")
        Integer pqacId
) {
}
