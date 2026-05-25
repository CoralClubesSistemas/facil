package com.coralclubes.facil.shared.infrastructure.gateway.filter;

import com.coralclubes.facil.shared.infrastructure.gateway.dto.UserInfo;
import com.coralclubes.facil.shared.infrastructure.gateway.service.GatewayAttributes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Filtro que lee los headers X-Auth-* inyectados por el API Gateway
 * y construye el SecurityContext para que los Controllers y Services
 * puedan acceder a la identidad del usuario sin tener que validar JWT.
 *
 * Este filtro SOLO se activa cuando las requests provienen del gateway.
 * Si un request llega directamente (sin X-Auth-Username), se rechaza.
 */
@RequiredArgsConstructor
public class GatewayHeaderFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private static final String USER_PROFILE_HEADER = "X-Auth-User-Profile";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Si ya hay autenticación establecida (flujo legacy), no interferir
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String profileJson = request.getHeader(USER_PROFILE_HEADER);

        // Si no viene el header de perfil, denegar acceso (no es un request válido del gateway)
        if (profileJson == null || profileJson.isBlank()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        UserInfo userInfo;

        try {
            userInfo = objectMapper.readValue(profileJson, UserInfo.class);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (userInfo.username() == null || userInfo.username().isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        List<String> permissions = userInfo.permissions() != null
                ? userInfo.permissions()
                : Collections.emptyList();

        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userInfo.username(),
                        null,
                        authorities
                );

        // Guardar objeto completo
        request.setAttribute(GatewayAttributes.USER_INFO, userInfo);

        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
