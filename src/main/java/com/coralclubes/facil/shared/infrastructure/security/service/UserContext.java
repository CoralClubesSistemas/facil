package com.coralclubes.facil.shared.infrastructure.security.service;

import com.coralclubes.facil.modules.sistema.dto.projection.ModuloDtoResult;
import com.coralclubes.facil.shared.infrastructure.gateway.dto.UserInfo;
import com.coralclubes.facil.shared.infrastructure.gateway.service.GatewayAttributes;
import com.coralclubes.facil.shared.infrastructure.security.dto.projection.UserAutorizacionesResult;
import com.coralclubes.facil.shared.infrastructure.security.repository.LoginRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Servicio de utilidad para acceder a la información del usuario autenticado.
 * <p>
 * El API Gateway valida el JWT y envía los headers X-Auth-*.
 * GatewayHeaderFilter lee esos headers y establece el SecurityContext.
 * Este servicio expone la identidad de forma limpia a los Services.
 * <p>
 * Si los permisos no vienen del gateway, se obtienen directamente desde la base de datos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserContext {

    private final LoginRepository loginRepository;

    private UserInfo getUserInfo() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) {
            return null;
        }

        Object value = attrs.getRequest().getAttribute(GatewayAttributes.USER_INFO);

        return value instanceof UserInfo userInfo ? userInfo : null;
    }

    /**
     * Obtiene el username del usuario autenticado.
     * El principal es un String (username) establecido por GatewayHeaderFilter.
     */
    public String getUsername() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.username() : "ANONYMOUS";
    }

    /**
     * Obtiene el id del desarrollo al que pertenece el usuario, si aplica.
     */
    public Integer getIdDesarrollo() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.idDesarrollo() : null;
    }

    /**
     * Obtiene el rol del usuario.
     */
    public String getRole() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.role() : null;
    }

    public Integer getRoleId() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.rolId() : null;
    }

    public String getSource() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.source() : null;
    }

    /**
     * Obtiene los permisos del usuario como lista de strings.
     * Si no están en el request attribute, los obtiene desde la base de datos.
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermissions() {
        UserInfo userInfo = getUserInfo();

        if (userInfo != null && userInfo.permissions() != null) {
            return userInfo.permissions();
        }

        return obtenerPermisosDesdeBD();
    }

    public String getEmail() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.email() : null;
    }

    public String getNombreCompleto() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.nombreCompleto() : null;
    }

    public String getStatus() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.status() : null;
    }

    private List<String> obtenerPermisosDesdeBD() {
        String username = getUsername();
        if (username == null || username.equals("ANONYMOUS") || username.equals("UNKNOWN")) {
            return Collections.emptyList();
        }

        try {
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

            log.debug("Permisos obtenidos de BD para usuario: {} ({} permisos)", username, permissions.size());
            return permissions;
        } catch (Exception e) {
            log.warn("Error al obtener permisos de BD para usuario {}: {}", username, e.getMessage());
            return Collections.emptyList();
        }
    }
}
