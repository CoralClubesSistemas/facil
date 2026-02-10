package com.coralclubes.facil.shared.infrastructure.security.controller;

import com.coralclubes.facil.modules.sistema.dto.response.ModuloApiResponse;
import com.coralclubes.facil.shared.infrastructure.security.dto.request.LoginRequest;
import com.coralclubes.facil.shared.infrastructure.security.dto.request.RefreshTokenRequest;
import com.coralclubes.facil.shared.infrastructure.security.dto.request.ValidacionAutorizacion;
import com.coralclubes.facil.shared.infrastructure.security.dto.response.AuthResponse;
import com.coralclubes.facil.shared.infrastructure.security.dto.response.RefreshTokenResponse;
import com.coralclubes.facil.shared.infrastructure.security.service.AuthService;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para manejar las solicitudes de autenticación y autorización.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserContext userContext;

    /**
     * Inicia sesión y devuelve tokens JWT (Access + Refresh).
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    /**
     * Renueva el token de acceso utilizando un Refresh Token válido.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    /**
     * Obtiene los módulos asignados a un usuario específico.
     */
    @GetMapping("/modulos")
    public ResponseEntity<ApiResponse<List<ModuloApiResponse>>> getModulosUsuario() {
        // Extraemos el nombre de usuario del contexto de seguridad
        String username = userContext.getUsername();

        return ResponseEntity.ok(authService.getModulosUsuario(username));
    }

    /**
     * Valida credenciales sin generar token (Login simple).
     * Útil para re-verificación de identidad en acciones sensibles.
     */
    @PostMapping("/login/simple")
    public ResponseEntity<ApiResponse<Boolean>> loginSimple(@Valid @RequestBody LoginRequest loginRequest) {
        ApiResponse<Boolean> response = authService.validarLoginSimple(loginRequest);
        return ResponseEntity.status(response.status()).body(response);
    }

    /**
     * Valida si un usuario tiene una autorización específica fuera de las políticas estándar.
     */
    @PostMapping("/validacion-autorizacion")
    public ResponseEntity<ApiResponse<Boolean>> validarAutorizacionUsuario(@Valid @RequestBody ValidacionAutorizacion request) {
        ApiResponse<Boolean> response = authService.validarAutorizacionFueraDePolitica(request);
        return ResponseEntity.status(response.status()).body(response);
    }
}