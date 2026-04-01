package com.coralclubes.facil.shared.infrastructure.security.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;

/**
 * Servicio de utilidad para acceder a la información del usuario autenticado.
 *
 * El API Gateway valida el JWT y envía los headers X-Auth-*.
 * GatewayHeaderFilter lee esos headers y establece el SecurityContext.
 * Este servicio expone la identidad de forma limpia a los Services.
 */
@Service
@RequiredArgsConstructor
public class UserContext {

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
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermissions() {
        Object attr = getRequestAttributeObject("X-Auth-Permissions");
        if (attr instanceof List<?> list) {
            return (List<String>) list;
        }
        return Collections.emptyList();
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
