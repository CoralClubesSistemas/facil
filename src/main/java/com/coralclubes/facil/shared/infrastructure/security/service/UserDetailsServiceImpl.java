package com.coralclubes.facil.shared.infrastructure.security.service;

import com.coralclubes.facil.modules.sistema.dto.projection.ModuloDtoResult;
import com.coralclubes.facil.shared.infrastructure.exceptions.custom.NoPermissionsException;
import com.coralclubes.facil.shared.infrastructure.exceptions.custom.NoWebRegistrationException;
import com.coralclubes.facil.shared.infrastructure.security.dto.projection.UserAutorizacionesResult;
import com.coralclubes.facil.shared.infrastructure.security.dto.projection.UserLoginResult;
import com.coralclubes.facil.shared.infrastructure.security.model.CustomUserDetails;
import com.coralclubes.facil.shared.infrastructure.security.repository.LoginRepository;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Servicio central de Spring Security.
 * Carga la información del usuario desde la BD
 * y la adapta al modelo de seguridad de Spring (CustomUserDetails).
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final LoginRepository loginRepository;
    private final BusinessLogger logger;

    /**
     * Carga el usuario para autenticación.
     * Este método es llamado automáticamente por el AuthenticationManager.
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        // 1. Obtener datos básicos del usuario
        // Si el usuario fue borrado de la BD mientras tenía un token válido, fallará aquí.
        UserLoginResult user = loginRepository.spLoginUsuarios(username)
                .orElseThrow(() -> new UsernameNotFoundException("Sesión inválida: El usuario no existe o fue eliminado."));

        // Si el usuario existe pero se le borró el password (se revocó acceso web),
        // bloqueamos el token aunque siga vigente en tiempo.
        if (user.password() == null || user.password().isBlank()) {
            logger.warn("SECURITY", "Intento de acceso con token válido pero usuario sin password: {}", username);
            throw new NoWebRegistrationException("Su cuenta no tiene acceso web activo.");
        }

        List<GrantedAuthority> authorities = extractAuthorities(user.usuario());

        return CustomUserDetails.builder()
                .username(user.usuario())
                .password(user.password())
                .authorities(authorities)
                .idDesarrollo(user.idDesarrollo())
                .desarrolloDescripcion(user.desarrolloDescripcion())
                .email(user.email())
                .rolId(user.rolId())
                .rolDescripcion(user.rolDescripcion())
                .build();
    }

    /**
     * Extrae las autorizaciones y las convierte a GrantedAuthority.
     */
    private List<GrantedAuthority> extractAuthorities(String username) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        List<ModuloDtoResult> modulos = loginRepository.spLoginModulosUsuarios(username);

        if (modulos.isEmpty()) {
            logger.warn("SECURITY", "Usuario '{}' tiene token pero cero módulos asignados en BD.", username);
            throw new NoPermissionsException("No tiene módulos asignados.");
        }

        modulos.stream()
                .map(ModuloDtoResult::clave)
                .filter(Objects::nonNull)
                .filter(clave -> !clave.isBlank())
                .forEach(clave ->
                        authorities.add(new SimpleGrantedAuthority("MOD_" + clave.toUpperCase()))
                );

        // 2. Cargar Autorizaciones fuera de política (Permisos Críticos)
        List<UserAutorizacionesResult> autorizaciones = loginRepository.spLoginObtenerAutorizacionesUsuario(username);

        autorizaciones.stream()
                .map(UserAutorizacionesResult::clave)
                .filter(Objects::nonNull)
                .filter(clave -> !clave.isBlank())
                .forEach(clave ->
                        authorities.add(new SimpleGrantedAuthority("AUTH_" + clave.toUpperCase()))
                );

        return authorities;
    }
}