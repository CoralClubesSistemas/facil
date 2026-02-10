package com.coralclubes.facil.shared.infrastructure.security.filter;

import com.coralclubes.facil.shared.infrastructure.security.jwt.JwtService;
import com.coralclubes.facil.shared.infrastructure.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticación JWT.
 * Intercepta las peticiones, extrae el token y autentica al usuario.
 * <p>
 * NOTA: Este filtro NO captura excepciones (try-catch). Deja que errores como
 * ExpiredJwtException burbujeen hacia el FilterExceptionHandler para generar
 * una respuesta JSON adecuada.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extraer token
        String jwt = parseJwt(request);

        // 2. Validar si hay token y si NO hay autenticación previa
        if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Si el token es inválido o expira aquí, JwtService lanzará una excepción
            // que será capturada por FilterExceptionHandler.
            String username = jwtService.getUsernameFromToken(jwt);

            if (username != null) {
                // Cargamos el usuario (verifica existencia y estado en BD)
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Validamos criptográficamente el token contra el usuario
                if (jwtService.validateToken(jwt, userDetails)) {

                    // Creamos la autenticación de Spring Security
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // Agregamos detalles de la petición (IP, Session ID)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Establecemos el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        // Continuamos la cadena
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token del encabezado Authorization.
     *
     * @param request La solicitud HTTP de la cual se extraerá el token.
     * @return El token JWT si está presente y bien formado, o null si no lo está.
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        // Tiene que comenzar con "Bearer " para ser considerado un token válido
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            // Retorna solo la parte del token, sin el prefijo "Bearer "
            return headerAuth.substring(7);
        }

        return null;
    }
}