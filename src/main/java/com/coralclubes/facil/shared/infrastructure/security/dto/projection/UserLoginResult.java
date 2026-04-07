package com.coralclubes.facil.shared.infrastructure.security.dto.projection;

import lombok.Builder;

/**
 * Representa el resultado directo del SP spLoginUsuarios.
 */
@Builder
public record UserLoginResult(
        String usuario,
        String password,
        Integer idDesarrollo,
        String desarrolloDescripcion,
        String email,
        Integer rolId,
        String rolDescripcion,
        String nombreCompleto
) {}