package com.coralclubes.facil.shared.infrastructure.security.service;

import com.coralclubes.facil.modules.sistema.dto.projection.ModuloDtoResult;
import com.coralclubes.facil.shared.infrastructure.security.dto.projection.UserAutorizacionesResult;
import com.coralclubes.facil.shared.infrastructure.security.repository.LoginRepository;
import jakarta.servlet.http.HttpServletRequest;
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
 *
 * El API Gateway valida el JWT y envía los headers X-Auth-*.
 * GatewayHeaderFilter lee esos headers y establece el SecurityContext.
 * Este servicio expone la identidad de forma limpia a los Services.
 *
 * Si los permisos no vienen del gateway, se obtienen directamente desde la base de datos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserContext {

    private final LoginRepository loginRepository;

    /**
     * Obtiene el username del usuario autenticado.
     * El principal es un String (username) establecido por GatewayHeaderFilter.
     */
    public String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return "ANONYMOUS";
        }

        Object principal = authentication.getPrincipal();
        return principal instanceof String s ? s : "UNKNOWN";
    }

    /**
     * Obtiene el ID del desarrollo/sucursal del usuario.
     * El gateway lo pasa como header X-Auth-LegacyId.
     */
    public Integer getIdDesarrollo() {
        String legacyId = getRequestAttribute("X-Auth-LegacyId");
        if (legacyId != null && !legacyId.isBlank()) {
            try {
                return Integer.parseInt(legacyId);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Obtiene el rol del usuario.
     */
    public String getRole() {
        return getRequestAttribute("X-Auth-Role");
    }

    /**
     * Obtiene el tipo de fuente del usuario (INTERNAL, EXTERNAL, SYSTEM).
     */
    public String getSource() {
        return getRequestAttribute("X-Auth-Source");
    }

    /**
     * Obtiene los permisos del usuario como lista de strings.
     * Si no están en el request attribute, los obtiene desde la base de datos.
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermissions() {
        Object attr = getRequestAttributeObject("X-Auth-Permissions");
        if (attr instanceof List<?> list) {
            return (List<String>) list;
        }

        return obtenerPermisosDesdeBD();
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

    private String getRequestAttribute(String name) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            Object value = request.getAttribute(name);
            return value != null ? value.toString() : null;
        }
        return null;
    }

    private Object getRequestAttributeObject(String name) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            return attrs.getRequest().getAttribute(name);
        }
        return null;
    }
}
