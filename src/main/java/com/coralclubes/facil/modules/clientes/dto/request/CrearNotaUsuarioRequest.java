package com.coralclubes.facil.modules.clientes.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearNotaUsuarioRequest(
        @NotBlank(message = "La membresía no puede estar vacía")
        String membresia,

        @NotNull(message = "La clasificación de nota es requerida")
        @Min(value = 1, message = "La clasificación debe ser mayor a 0")
        Integer clasificacionNota,

        @NotBlank(message = "La nota no puede estar vacía")
        String nota,

        @NotNull(message = "El indicador de alerta es requerido")
        Boolean alerta
) {
}

