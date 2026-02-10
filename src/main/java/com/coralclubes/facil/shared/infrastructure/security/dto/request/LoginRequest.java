package com.coralclubes.facil.shared.infrastructure.security.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO que representa una solicitud de inicio de sesión,
 * definiendo que el cuerpo de la solicitud debe contener
 * el nombre de usuario y la contraseña.
 */
public record LoginRequest(
        @NotBlank(message = "El nombre de usuario es requerido")
        String username,

        @NotBlank(message = "La contraseña es requerida")
        String password
) {
}