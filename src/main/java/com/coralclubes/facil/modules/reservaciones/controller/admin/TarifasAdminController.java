package com.coralclubes.facil.modules.reservaciones.controller.admin;

import com.coralclubes.facil.modules.reservaciones.dto.request.EliminarTarifasRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.TarifasWrapperRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.TarifaDto;
import com.coralclubes.facil.modules.reservaciones.service.TarifasService;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST Administrativo para la gestión de Tarifas Base.
 * Exclusivo para el sistema interno (Facil Core). Los portales web
 * deberán usar el motor de cotización para calcular precios finales.
 */
@RestController
@RequestMapping("/api/v1/admin/reservaciones/tarifas")
@RequiredArgsConstructor
public class TarifasAdminController {

    private final TarifasService service;

    // =========================================================================
    // LECTURA (Solo Admin)
    // =========================================================================

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<TarifaDto>>> obtenerTarifas(
            @RequestParam(required = false) Integer anio) {
        return ResponseEntity.ok(service.obtenerTarifas(anio));
    }

    // =========================================================================
    // ESCRITURA
    // =========================================================================

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('MOD_SMNUTARIFASRESERVACIONES')")
    public ResponseEntity<ApiResponse<Integer>> guardarTarifas(
            @Valid @RequestBody TarifasWrapperRequest request) {

        ApiResponse<Integer> response = service.guardarTarifas(request.tarifas());
        return ResponseEntity.status(response.status()).body(response);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('MOD_SMNUTARIFASRESERVACIONES')")
    public ResponseEntity<ApiResponse<Integer>> eliminarTarifas(
            @Valid @RequestBody EliminarTarifasRequest request) {

        ApiResponse<Integer> response = service.eliminarTarifas(request.ids());
        return ResponseEntity.status(response.status()).body(response);
    }
}