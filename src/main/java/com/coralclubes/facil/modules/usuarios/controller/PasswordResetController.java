package com.coralclubes.facil.modules.usuarios.controller;

import com.coralclubes.facil.modules.usuarios.dto.request.PasswordResetConfirmRequest;
import com.coralclubes.facil.modules.usuarios.dto.request.PasswordResetRequest;
import com.coralclubes.facil.modules.usuarios.service.PasswordResetService;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * Endpoint para solicitar un restablecimiento de contraseña.
     * Recibe un objeto PasswordResetRequest con el correo electrónico y nombre de usuario del usuario,
     * y envía un enlace de recuperación al correo electrónico proporcionado.
     *
     * @param request Objeto que contiene el correo electrónico y nombre de usuario del usuario.
     * @return ApiResponse con un mensaje de éxito o error.
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Boolean>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        ApiResponse<Boolean> response = passwordResetService.requestPasswordReset(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    /**
     * Endpoint para validar un token de restablecimiento de contraseña.
     * Recibe un token como parámetro y verifica su validez.
     *
     * @param token Token de restablecimiento de contraseña a validar.
     * @return ApiResponse con un mensaje de éxito o error.
     */
    @PostMapping("/validate-token")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestParam String token) {
        ApiResponse<Boolean> response = passwordResetService.validateToken(token);
        return ResponseEntity.status(response.status()).body(response);
    }

    /**
     * Endpoint para confirmar el restablecimiento de contraseña.
     * Recibe un objeto PasswordResetConfirmRequest con el token y la nueva contraseña,
     * y actualiza la contraseña del usuario si el token es válido.
     *
     * @param request Objeto que contiene el token de restablecimiento y la nueva contraseña.
     * @return ResponseEntity con un mensaje de éxito o error.
     */
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Boolean>> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        ApiResponse<Boolean> response = passwordResetService.resetPassword(request);
        return ResponseEntity.status(response.status()).body(response);
    }
}