package com.coralclubes.facil.modules.reservaciones.controller.admin;

import com.coralclubes.facil.modules.reservaciones.dto.request.CampanaPuntosRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.CampanaPuntosResponse;
import com.coralclubes.facil.modules.reservaciones.service.CampanasPuntosService;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitarUrlImagenRequest;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/promociones-puntos")
@RequiredArgsConstructor
public class CampanasPuntosAdminController {

    private final CampanasPuntosService service;

    @GetMapping
    @PreAuthorize("hasAuthority('MOD_SMNURESERVACIONES')")
    public ResponseEntity<ApiResponse<List<CampanaPuntosResponse>>> obtenerCampanas() {
        return ResponseEntity.ok(service.obtenerCampanas());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MOD_SMNURESERVACIONES')")
    public ResponseEntity<ApiResponse<Integer>> guardarCampana(
            @Valid @RequestBody CampanaPuntosRequest request) {
        return ResponseEntity.ok(service.guardarCampana(request));
    }

    @DeleteMapping("/{idPromocion}")
    @PreAuthorize("hasAuthority('MOD_SMNURESERVACIONES')")
    public ResponseEntity<ApiResponse<Boolean>> eliminarCampana(
            @PathVariable Integer idPromocion) {
        return ResponseEntity.ok(service.eliminarCampana(idPromocion));
    }

    @PostMapping("/url-carga")
    @PreAuthorize("hasAuthority('MOD_SMNURESERVACIONES')")
    public ResponseEntity<ApiResponse<RespuestaCargaDto>> solicitarUrlCarga(
            @RequestBody SolicitarUrlImagenRequest request) {
        return ResponseEntity.ok(service.solicitarUrlCarga(request));
    }
}