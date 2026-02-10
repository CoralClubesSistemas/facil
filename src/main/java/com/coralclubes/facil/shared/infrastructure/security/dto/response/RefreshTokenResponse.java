package com.coralclubes.facil.shared.infrastructure.security.dto.response;

/**
 * DTO que representa la respuesta de un nuevo token y refresh token
 */
public record RefreshTokenResponse(
        String token,
        String refreshToken
) {}