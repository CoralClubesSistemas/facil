package com.coralclubes.facil.modules.reservaciones.controller.admin;

import com.coralclubes.facil.modules.reservaciones.dto.request.TemporadaMasivaWrapperRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.TemporadaRequest;
import com.coralclubes.facil.modules.reservaciones.service.TemporadasService;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST Administrativo para el Submódulo de Temporadas.
 * Proporciona los endpoints de escritura que requieren autenticación y permisos.
 */
@RestController
@RequestMapping("/api/v1/admin/reservaciones/temporadas")
@RequiredArgsConstructor
public class TemporadasAdminController {

    private final TemporadasService service;

    // =========================================================================
    // ENDPOINTS DE ESCRITURA (POST / DELETE)
    // =========================================================================

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('MOD_SMNUTEMPORADASRESERVACIONES')")
    public ResponseEntity<ApiResponse<Integer>> guardarTemporada(
            @Valid @RequestBody TemporadaRequest request) {

        ApiResponse<Integer> response = service.guardarTemporada(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('MOD_SMNUTEMPORADASRESERVACIONES')")
    public ResponseEntity<ApiResponse<Boolean>> eliminarTemporada(
            @RequestParam Integer idTemporadaFecha) {

        ApiResponse<Boolean> response = service.eliminarTemporada(idTemporadaFecha);
        return ResponseEntity.status(response.status()).body(response);
    }

    @PostMapping("/save-masivo")
    @PreAuthorize("hasAuthority('MOD_SMNUTEMPORADASRESERVACIONES')")
    public ResponseEntity<ApiResponse<Integer>> guardarTemporadasMasivas(
            @Valid @RequestBody TemporadaMasivaWrapperRequest request) {

        ApiResponse<Integer> response = service.guardarTemporadasMasivas(request.temporadas());
        return ResponseEntity.status(response.status()).body(response);
    }
}