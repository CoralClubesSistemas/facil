package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgregarOtaRequest(
        @NotBlank(message = "El nombre de la OTA es obligatorio")
        @Size(max = 100, message = "El nombre de la OTA no puede superar los 100 caracteres")
        String nombreOta
) {
}
