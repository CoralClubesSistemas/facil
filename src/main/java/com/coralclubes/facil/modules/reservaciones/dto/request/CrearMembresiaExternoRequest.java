package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearMembresiaExternoRequest(
        @NotNull(message = "El desarrollo es obligatorio") Integer desarrollo,
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        String segundoNombre,
        @NotBlank(message = "El apellido paterno es obligatorio") String apellidoPaterno,
        String apellidoMaterno,
        @NotBlank(message = "El email principal es obligatorio") String emailPrincipal,
        @NotBlank(message = "El teléfono principal es obligatorio") String telefonoPrincipal,
        String emailSecundario,
        String telefonoSecundario
) {}
