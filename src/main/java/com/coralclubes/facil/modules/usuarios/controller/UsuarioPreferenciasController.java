package com.coralclubes.facil.modules.usuarios.controller;

import com.coralclubes.facil.modules.usuarios.dto.request.ActualizarPreferenciasRequest;
import com.coralclubes.facil.modules.usuarios.dto.response.PreferenciasResponse;
import com.coralclubes.facil.modules.usuarios.service.UsuarioService;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador para la gestión de preferencias del usuario autenticado.
 */
@RestController
@RequestMapping("/api/v1/admin/usuarios/preferencias")
@RequiredArgsConstructor
public class UsuarioPreferenciasController {

    private final UsuarioService usuarioService;
    private final UserContext userContext;

    /**
     * Obtiene las preferencias del usuario actual.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PreferenciasResponse>> obtenerPreferencias() {
        String username = userContext.getUsername();
        Map<String, Object> preferencias = usuarioService.obtenerPreferenciasMap(username);
        return ResponseEntity.ok(ApiResponse.success(new PreferenciasResponse(preferencias)));
    }

    /**
     * Actualiza o agrega preferencias del usuario actual.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> actualizarPreferencias(
            @Valid @RequestBody ActualizarPreferenciasRequest request) {
        String username = userContext.getUsername();
        usuarioService.actualizarPreferenciasMap(username, request.preferencias());
        return ResponseEntity.ok(ApiResponse.success(true));
    }
}
