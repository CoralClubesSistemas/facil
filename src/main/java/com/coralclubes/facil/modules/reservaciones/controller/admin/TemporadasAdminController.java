package com.coralclubes.facil.modules.reservaciones.controller.admin;

import com.coralclubes.facil.modules.reservaciones.dto.request.TemporadaMasivaWrapperRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.TemporadaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.TemporadaDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.TemporadaFechaResponse;
import com.coralclubes.facil.modules.reservaciones.service.TemporadasService;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
    // ENDPOINTS DE LECTURA (admin, equivalente a public)
    // =========================================================================

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<TemporadaDto>>> obtenerTemporadas(
            @RequestParam(required = false) Integer anio) {
        return ResponseEntity.ok(service.obtenerTemporadas(anio));
    }

    @GetMapping("/fecha")
    public ResponseEntity<ApiResponse<List<TemporadaFechaResponse>>> obtenerTemporadasPorFecha(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(service.obtenerTemporadasPorFecha(fecha));
    }

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