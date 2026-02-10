package com.coralclubes.facil.shared.infrastructure.security.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO que representa una solicitud de validación de autorización.
 * es decir, definimos que, como cuerpo de la solicitud
 * se debe recibir el nombre de usuario, la contraseña
 * y el nombre (clave) de la autorización a validar
 * */
public record ValidacionAutorizacion(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String autorizacion
) {}