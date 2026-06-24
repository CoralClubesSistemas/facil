package com.coralclubes.facil.modules.usuarios.controller;

import com.coralclubes.facil.modules.usuarios.dto.request.ValidacionAutorizacion;
import com.coralclubes.facil.modules.usuarios.service.AutorizacionService;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para validación de autorizaciones fuera de política.
 *
 * Re-valida la identidad del usuario mediante su contraseña
 * y verifica si tiene una autorización específica asignada.
 *
 * El gateway valida el JWT e inyecta X-Auth-* headers.
 * El @PreAuthorize opcional puede usarse si se requiere
 * un permiso base para acceder a esta validación.
 */
@RestController
@RequestMapping("/api/v1/admin/autorizacion")
@RequiredArgsConstructor
public class AutorizacionController {

    private final AutorizacionService autorizacionService;

    /**
     * Valida si un usuario tiene una autorización específica.
     * El usuario debe enviar su contraseña para re-validar su identidad.
     */
    @PostMapping("/validar")
    public ResponseEntity<ApiResponse<Boolean>> validarAutorizacion(
            @Valid @RequestBody ValidacionAutorizacion request
    ) {
        ApiResponse<Boolean> response = autorizacionService.validarAutorizacionFueraDePolitica(request);
        return ResponseEntity.status(response.status()).body(response);
    }
}
