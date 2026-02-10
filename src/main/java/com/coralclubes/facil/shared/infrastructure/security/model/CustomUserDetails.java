package com.coralclubes.facil.shared.infrastructure.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Extensión de UserDetails para incluir información específica del negocio
 * que será transportada en el contexto de seguridad de Spring.
 */
@Getter
@Builder
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    // Campos estándar de Spring Security
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    // Campos personalizados del sistema FACIL
    private final Integer idDesarrollo;
    private final String desarrolloDescripcion;
    private final String email;
    private final Integer rolId;
    private final String rolDescripcion;

    // --- Implementación de métodos UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}