package com.coralclubes.facil.shared.infrastructure.security.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Clase que representa un token de restablecimiento de contraseña.
 * Contiene el correo electrónico del usuario, el token y la fecha de expiración.
 */
@Builder
public record PasswordResetToken(
        String username,
        String email,
        String token,
        LocalDateTime expiryDate
) {
}