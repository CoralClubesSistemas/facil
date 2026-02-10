package com.coralclubes.facil.shared.infrastructure.security.service;

import com.coralclubes.facil.shared.infrastructure.security.dto.response.AuthResponse;
import com.coralclubes.facil.shared.infrastructure.security.model.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio de utilidad para acceder a la información del usuario autenticado
 * almacenada en el contexto de seguridad de Spring (ThreadLocal).
 */
@Service
@RequiredArgsConstructor
public class UserContext {

    /**
     * Obtiene el objeto CustomUserDetails completo del contexto de seguridad.
     *
     * @return Un Optional que contiene los detalles del usuario si está autenticado.
     */
    public Optional<CustomUserDetails> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Pattern Matching: verifica tipo y asigna variable en un solo paso
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return Optional.of(userDetails);
        }

        return Optional.empty();
    }

    /**
     * Obtiene solo el username (subject).
     * Maneja tanto CustomUserDetails como strings simples (ej. "anonymousUser").
     */
    public String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return "ANONYMOUS";
        }

        Object principal = authentication.getPrincipal();

        return switch (principal) {
            case UserDetails u -> u.getUsername();
            case String s -> s; // Caso para usuarios anónimos o tokens simples
            default -> "UNKNOWN";
        };
    }

    /**
     * Obtiene el ID del desarrollo actual del usuario.
     * Útil para filtrar consultas de base de datos automáticamente.
     */
    public Integer getIdDesarrollo() {
        return getCurrentUser()
                .map(CustomUserDetails::getIdDesarrollo)
                .orElse(null); // O lanzar una excepción si el contexto es obligatorio
    }

    /**
     * Construye un AuthResponse basado en el usuario actual.
     * NOTA: El token y refresh token no suelen estar en el UserDetails,
     * por lo que se devolverán nulos o vacíos aquí.
     */
    public Optional<AuthResponse> getAuthResponseActual() {
        return getCurrentUser().map(user -> AuthResponse.builder()
                .usuario(user.getUsername())
                .idDesarrollo(user.getIdDesarrollo())
                .email(user.getEmail())
                .rolId(user.getRolId())
                .rolDescripcion(user.getRolDescripcion())
                // El token no se recupera del contexto, ya se validó
                .build());
    }
}