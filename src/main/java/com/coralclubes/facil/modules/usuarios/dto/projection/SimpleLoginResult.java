package com.coralclubes.facil.modules.usuarios.dto.projection;

import lombok.Builder;

/**
 * Proyección para el resultado de spLoginSimple.
 * Retorna solo usuario y password hash para validaciones de credenciales.
 */
@Builder
public record SimpleLoginResult(
        String usuario,
        String password
) {}
