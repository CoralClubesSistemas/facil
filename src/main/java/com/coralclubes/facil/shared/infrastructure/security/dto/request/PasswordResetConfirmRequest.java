package com.coralclubes.facil.shared.infrastructure.security.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para solicitar el restablecimiento de contraseña.
 * Contiene el token de restablecimiento, la nueva contraseña
 * y la confirmación de la contraseña.
 */
public record PasswordResetConfirmRequest(
        @NotBlank(message = "El token es requerido")
        String token,

        @NotBlank(message = "La nueva contraseña es requerida")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String newPassword,

        @NotBlank(message = "La confirmación de contraseña es requerida")
        String confirmPassword
) {
}