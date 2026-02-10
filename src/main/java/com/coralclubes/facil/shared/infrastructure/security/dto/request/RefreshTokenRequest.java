package com.coralclubes.facil.shared.infrastructure.security.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para solicitar un nuevo token de acceso
 * utilizando un token de refresco (refresh token).
 */
public record RefreshTokenRequest(
        @NotBlank(message = "El refresh token es requerido")
        String refreshToken
) {}