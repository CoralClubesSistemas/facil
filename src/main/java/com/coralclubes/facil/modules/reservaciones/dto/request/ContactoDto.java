package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactoDto(
        @NotBlank(message = "El nombre es requerido")
        @Size(max = 100, message = "El nombre no debe exceder los 100 caracteres")
        String nombre,

        @NotBlank(message = "El email es requerido")
        @Email(message = "El email debe ser un formato válido")
        @Size(max = 100, message = "El email no debe exceder los 100 caracteres")
        String email,

        @Size(max = 20, message = "El teléfono no debe exceder los 20 caracteres")
        String telefono,

        @Size(max = 100, message = "El hotel de interés no debe exceder los 100 caracteres")
        String hotelInteres,

        @NotBlank(message = "El mensaje es requerido")
        @Size(max = 1000, message = "El mensaje no debe exceder los 1000 caracteres")
        String mensaje
) {}
