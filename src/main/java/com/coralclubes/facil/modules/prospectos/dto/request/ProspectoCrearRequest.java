package com.coralclubes.facil.modules.prospectos.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * Request DTO para crear o actualizar un prospecto de venta.
 */
@Builder
public record ProspectoCrearRequest(
        @NotBlank(message = "El ID externo del prospecto es obligatorio")
        String idExterno,

        String origen,

        @NotBlank(message = "El nombre del prospecto es obligatorio")
        String nombre,

        String segundoNombre,

        @NotBlank(message = "El apellido paterno del prospecto es obligatorio")
        String apellidoPaterno,

        String apellidoMaterno,

        @Email(message = "El formato de correo electrónico no es válido")
        String email,

        String telefono,

        String cargo,

        Integer edad,

        Integer desarrolloInteres,

        Integer lsvAreaInteres,

        String dataAdicional,

        Integer estatus
) {
}
