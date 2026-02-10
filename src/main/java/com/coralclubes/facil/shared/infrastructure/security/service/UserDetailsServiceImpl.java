package com.coralclubes.facil.shared.infrastructure.security.service;

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

        // 3. Cargar permisos
        List<GrantedAuthority> authorities = extractAuthorities(user.usuario());

        // 4. Construir el objeto UserDetails
        return CustomUserDetails.builder()
                .username(user.usuario())
                .password(user.password()) // Se requiere por contrato, aunque el filtro JWT no lo usa para comparar.
                .authorities(authorities)
                // Metadatos para el UserContext
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
        List<UserAutorizacionesResult> rawPermissions = loginRepository.spLoginObtenerAutorizacionesUsuario(username);

        // Si el usuario se quedó sin roles, no debería poder operar nada.
        if (rawPermissions.isEmpty()) {
            logger.warn("SECURITY", "Usuario '{}' tiene token pero cero permisos en BD.", username);
            throw new NoPermissionsException("No tiene permisos asignados.");
        }

        return rawPermissions.stream()
                .map(UserAutorizacionesResult::clave)
                .filter(Objects::nonNull)
                .map(clave -> new SimpleGrantedAuthority("ACCESS_" + clave))
                .map(GrantedAuthority.class::cast)
                .toList();
    }
}