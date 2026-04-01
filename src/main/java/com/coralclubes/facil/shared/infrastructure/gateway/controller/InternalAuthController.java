package com.coralclubes.facil.shared.infrastructure.gateway.controller;

import com.coralclubes.facil.shared.infrastructure.gateway.dto.UserInfo;
import com.coralclubes.facil.shared.infrastructure.gateway.service.InternalAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint interno consumido EXCLUSIVAMENTE por el API Gateway.
 * NO es accesible desde el frontend.
 */
@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
public class InternalAuthController {

    private final InternalAuthService internalAuthService;

    /**
     * Valida credenciales y retorna UserInfo completo.
     * Usado durante el login.
     */
    @PostMapping("/login")
    public ResponseEntity<UserInfo> login(@RequestBody InternalLoginRequest request) {
        UserInfo userInfo = internalAuthService.autenticar(request.username(), request.password());
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Retorna UserInfo completo por username (sin validar password).
     * Usado durante el refresh token — el gateway ya validó la identidad
     * via el refresh token JWT, solo necesita los datos completos del usuario.
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<UserInfo> getUserByUsername(@PathVariable String username) {
        UserInfo userInfo = internalAuthService.obtenerPorUsername(username);
        return ResponseEntity.ok(userInfo);
    }

    public record InternalLoginRequest(
            String username,
            String password
    ) {}
}
