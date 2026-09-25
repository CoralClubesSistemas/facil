package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GuardarImagenPortalCompraMembresiaRequest(
        @NotBlank(message = "La imagen o UUID es obligatorio")
        @Size(max = 200, message = "La imagen no puede exceder 200 caracteres")
        String img
) {}
