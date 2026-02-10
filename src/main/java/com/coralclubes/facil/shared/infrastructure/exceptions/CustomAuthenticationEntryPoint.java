package com.coralclubes.facil.shared.infrastructure.exceptions;

import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.responses.codes.GeneralResponseCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * CustomAuthenticationEntryPoint es una clase que implementa AuthenticationEntryPoint
 * para manejar los casos de acceso no autorizado (401) en el Sistema FACIL.
 * Cuando un usuario intenta acceder a un recurso protegido sin autenticación válida,
 * este componente se encarga de enviar una respuesta JSON con un mensaje de error claro y un código de respuesta específico,
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final BusinessLogger businessLogger;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        // Logueamos como advertencia de seguridad
        businessLogger.warn("FILTER", "Acceso denegado (401) en filtro: {}", request.getRequestURI());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        ApiResponse<?> apiResponse = ApiResponse.error(
                GeneralResponseCode.UNAUTHORIZED,
                "Acceso denegado: Token faltante o inválido."
        );

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}