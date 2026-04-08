package com.coralclubes.facil.shared.infrastructure.gateway.service;

import com.coralclubes.facil.modules.sistema.dto.projection.ModuloDtoResult;
import com.coralclubes.facil.shared.infrastructure.exceptions.custom.NoWebRegistrationException;
import com.coralclubes.facil.shared.infrastructure.gateway.dto.UserInfo;
import com.coralclubes.facil.shared.infrastructure.security.dto.projection.UserAutorizacionesResult;
import com.coralclubes.facil.shared.infrastructure.security.dto.projection.UserLoginResult;
import com.coralclubes.facil.shared.infrastructure.security.repository.LoginRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

/**
 * Servicio de autenticación interna para el gateway.
 * Valida credenciales contra los SPs del sistema y retorna
 * el UserInfo estandarizado que el gateway usa para generar JWTs.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InternalAuthService {

    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;

    public UserInfo autenticar(String username, String password) {
        // 1. Buscar usuario via SP
        UserLoginResult userData = loginRepository.spLoginUsuarios(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        // 2. Validar que tenga password web activo
        if (userData.password() == null || userData.password().isBlank()) {
            throw new NoWebRegistrationException("El usuario no tiene registro web activo");
        }

        // 3. Validar contraseña (BCrypt)
        if (!passwordEncoder.matches(password, userData.password())) {
            throw new BadCredentialsException("Contraseña incorrecta");
        }

        // 4. Cargar módulos (permisos del sistema)
        List<ModuloDtoResult> modulos = loginRepository.spLoginModulosUsuarios(userData.usuario());

        // 5. Cargar autorizaciones fuera de política
        List<UserAutorizacionesResult> autorizaciones = loginRepository.spLoginObtenerAutorizacionesUsuario(userData.usuario());

        // 6. Construir lista unificada de permisos
        List<String> permissions = Stream.concat(
                modulos.stream()
                        .filter(m -> m.clave() != null)
                        .map(m -> "MOD_" + m.clave().toUpperCase()),
                autorizaciones.stream()
                        .filter(a -> a.clave() != null)
                        .map(a -> "AUTH_" + a.clave().toUpperCase())
        ).distinct().toList();

        // 7. Determinar source type
        String source = determinarSourceType(userData);

        log.info("Autenticación interna exitosa para usuario: {} [{}]", userData.usuario(), source);

        // 8. Retornar UserInfo estandarizado
        return UserInfo.builder()
                .username(userData.usuario())
                .email(userData.email())
                .role(userData.rolDescripcion())
                .source(source)
                .legacyId(userData.idDesarrollo() != null ? String.valueOf(userData.idDesarrollo()) : null)
                .status("REGISTERED")
                .idDesarrollo(userData.idDesarrollo())
                .desarrolloDescripcion(userData.desarrolloDescripcion())
                .rolId(userData.rolId())
                .permissions(permissions)
                .nombreCompleto(userData.nombreCompleto())
                .build();
    }

    private String determinarSourceType(UserLoginResult userData) {
        if (userData.rolDescripcion() == null) {
            return "SYSTEM";
        }
        String rol = userData.rolDescripcion().toUpperCase();
        if (rol.contains("SOCIO") || rol.contains("CLIENTE")) {
            return "EXTERNAL";
        }
        return "INTERNAL";
    }

    /**
     * Retorna UserInfo completo por username SIN validar password.
     * Usado durante el refresh token — el gateway ya validó la identidad.
     */
    public UserInfo obtenerPorUsername(String username) {
        UserLoginResult userData = loginRepository.spLoginUsuarios(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado: " + username));

        List<ModuloDtoResult> modulos = loginRepository.spLoginModulosUsuarios(username);
        List<UserAutorizacionesResult> autorizaciones = loginRepository.spLoginObtenerAutorizacionesUsuario(username);

        List<String> permissions = Stream.concat(
                modulos.stream()
                        .filter(m -> m.clave() != null)
                        .map(m -> "MOD_" + m.clave().toUpperCase()),
                autorizaciones.stream()
                        .filter(a -> a.clave() != null)
                        .map(a -> "AUTH_" + a.clave().toUpperCase())
        ).distinct().toList();

        return UserInfo.builder()
                .username(userData.usuario())
                .email(userData.email())
                .role(userData.rolDescripcion())
                .source(determinarSourceType(userData))
                .legacyId(userData.idDesarrollo() != null ? String.valueOf(userData.idDesarrollo()) : null)
                .status("REGISTERED")
                .idDesarrollo(userData.idDesarrollo())
                .desarrolloDescripcion(userData.desarrolloDescripcion())
                .rolId(userData.rolId())
                .permissions(permissions)
                .build();
    }

    /**
     * Retorna solo la lista de permisos del usuario.
     * Usado por UserContext cuando el gateway no envía permisos en headers.
     */
    public List<String> obtenerPermisosPorUsername(String username) {
        List<ModuloDtoResult> modulos = loginRepository.spLoginModulosUsuarios(username);
        List<UserAutorizacionesResult> autorizaciones = loginRepository.spLoginObtenerAutorizacionesUsuario(username);

        return Stream.concat(
                modulos.stream()
                        .filter(m -> m.clave() != null)
                        .map(m -> "MOD_" + m.clave().toUpperCase()),
                autorizaciones.stream()
                        .filter(a -> a.clave() != null)
                        .map(a -> "AUTH_" + a.clave().toUpperCase())
        ).distinct().toList();
    }
}
