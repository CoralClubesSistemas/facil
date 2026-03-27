package com.coralclubes.facil.shared.infrastructure.security.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO que representa una solicitud de validación de autorización fuera de política.
 * El usuario envía sus credenciales para re-validar identidad
 * y la clave de la autorización que desea verificar.
 */
public record ValidacionAutorizacion(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String autorizacion
) {}
