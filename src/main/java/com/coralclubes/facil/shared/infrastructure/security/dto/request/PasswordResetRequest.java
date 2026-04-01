package com.coralclubes.facil.shared.infrastructure.security.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO que representa una solicitud de restablecimiento de contraseña
 * es decir, definimos que, como cuerpo de la solicitud
 * se debe recibir el correo electrónico del usuario
 */
public record PasswordResetRequest(
        @NotBlank(message = "El nombre de usuario es requerido")
        String username,

        @NotBlank(message = "El correo electrónico es requerido")
        @Email(message = "El correo electrónico debe ser válido")
        String email
) {}