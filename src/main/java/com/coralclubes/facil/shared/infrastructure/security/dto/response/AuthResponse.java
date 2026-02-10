package com.coralclubes.facil.shared.infrastructure.security.dto.response;

import lombok.Builder;

/**
 * Respuesta principal de autenticación.
 */
@Builder
public record AuthResponse(
        String token,
        String refreshToken,
        String usuario,
        Integer idDesarrollo,
        String desarrolloDescripcion,
        String email,
        Integer rolId,
        String rolDescripcion
) {}