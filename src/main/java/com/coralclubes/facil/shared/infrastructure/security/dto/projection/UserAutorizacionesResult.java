package com.coralclubes.facil.shared.infrastructure.security.dto.projection;

import lombok.Builder;

/**
 * Representa el resultado del SP que consulta las autorizaciones de un usuario.
 */
@Builder
public record UserAutorizacionesResult(
        Integer id,
        String nombre,
        String clave
) {}