package com.coralclubes.facil.shared.infrastructure.gateway.filter;

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
@Component
@RequiredArgsConstructor
public class GatewayHeaderFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

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

        String username = request.getHeader("X-Auth-Username");

        // Sin header X-Auth-Username = request no vino del gateway
        // (las rutas públicas ya fueron excluidas por SecurityConfig)
        if (username == null || username.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Leer permisos del header (JSON array como string)
        // Si no viene del gateway, UserContext los obtendrá desde BD
        List<String> permissions = parsePermissions(request.getHeader("X-Auth-Permissions"));

        // Construir GrantedAuthorities para @PreAuthorize
        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .map(a -> (SimpleGrantedAuthority) a)
                .toList();

        // Crear Authentication y establecer en SecurityContext
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities
        );

        // Agregar datos adicionales como atributos del request
        // para que UserContext pueda leerlos sin depender de CustomUserDetails
        request.setAttribute("X-Auth-Username", username);
        request.setAttribute("X-Auth-Role", request.getHeader("X-Auth-Role"));
        request.setAttribute("X-Auth-Source", request.getHeader("X-Auth-Source"));
        request.setAttribute("X-Auth-LegacyId", request.getHeader("X-Auth-LegacyId"));
        request.setAttribute("X-Auth-System", request.getHeader("X-Auth-System"));
        request.setAttribute("X-Auth-Permissions", permissions);

        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private List<String> parsePermissions(String permissionsJson) {
        if (permissionsJson == null || permissionsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(permissionsJson, new TypeReference<>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
