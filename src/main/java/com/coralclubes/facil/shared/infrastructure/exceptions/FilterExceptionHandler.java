package com.coralclubes.facil.shared.infrastructure.exceptions;

import com.coralclubes.BaseException;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.responses.codes.GeneralResponseCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que envuelve la cadena de seguridad para capturar excepciones
 * ocurridas en filtros posteriores (como JwtAuthenticationFilter) y
 * formatearlas como ApiResponse JSON.
 */
@Component
@RequiredArgsConstructor
public class FilterExceptionHandler extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final BusinessLogger businessLogger;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            handleFilterException(e, response);
        }
    }

    private void handleFilterException(Exception e, HttpServletResponse response) throws IOException {
        businessLogger.error("SYSTEM", "Excepción en Filtro de Seguridad: " + e.getMessage(), e);

        response.setContentType("application/json");
        ApiResponse<?> apiResponse;

        // 1. Manejo de Excepciones propias
        if (e instanceof BaseException baseEx) {
            response.setStatus(baseEx.getResponseCode().getStatus());
            apiResponse = ApiResponse.error(baseEx.getResponseCode(), baseEx.getMessage());
        }
        // 2. Manejo específico de JWT
        else if (e instanceof ExpiredJwtException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            apiResponse = ApiResponse.error(GeneralResponseCode.UNAUTHORIZED, "El token ha expirado.");
        } else if (e instanceof MalformedJwtException || e instanceof SignatureException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            apiResponse = ApiResponse.error(GeneralResponseCode.UNAUTHORIZED, "Token corrupto o firma inválida.");
        }
        // 3. Error genérico
        else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            apiResponse = ApiResponse.error(GeneralResponseCode.INTERNAL_SERVER_ERROR, "Error interno en filtro de seguridad.");
        }

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}