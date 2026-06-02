package com.coralclubes.facil.shared.infrastructure.security.service;

import com.coralclubes.facil.shared.infrastructure.gateway.dto.UserInfo;
import com.coralclubes.facil.shared.infrastructure.gateway.service.GatewayAttributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

    // Devuelve el tipo de usuario (INTERNAL, EXTERNAL, SYSTEM)
    public String getSource() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.source() : null;
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
}
