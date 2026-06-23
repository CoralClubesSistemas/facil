package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GuardarExperienciaRequest(
        Integer id,
        @NotBlank(message = "El tag es requerido")
        @Size(max = 50, message = "El tag no debe exceder los 50 caracteres")
        String tag,
        @NotBlank(message = "El título es requerido")
        @Size(max = 100, message = "El título no debe exceder los 100 caracteres")
        String titulo,
        @NotBlank(message = "La descripción es requerida")
        @Size(max = 500, message = "La descripción no debe exceder los 500 caracteres")
        String descripcion,
        @Size(max = 200, message = "El link no debe exceder los 200 caracteres")
        String link,
        @Size(max = 200, message = "La imagen no debe exceder los 200 caracteres")
        String img
) {}
