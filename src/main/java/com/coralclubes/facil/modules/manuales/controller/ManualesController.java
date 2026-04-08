package com.coralclubes.facil.modules.manuales.controller;

import com.coralclubes.facil.modules.manuales.dto.request.ManualRequest;
import com.coralclubes.facil.modules.manuales.dto.request.VersionRequest;
import com.coralclubes.facil.modules.manuales.dto.response.ManualResponse;
import com.coralclubes.facil.modules.manuales.dto.response.VersionResponse;
import com.coralclubes.facil.modules.manuales.service.ManualesService;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitarUrlRequest;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/manuales")
@RequiredArgsConstructor
public class ManualesController {

    private final ManualesService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ManualResponse>>> listar(
            @RequestParam(required = false) Integer moduloPadreId,
            @RequestParam(required = false) Integer moduloId,
            @RequestParam(required = false, defaultValue = "1") Integer numeroPagina) {

        return ResponseEntity.ok(service.listarManuales(moduloPadreId, moduloId, numeroPagina));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MOD_MNUCONTROLDELSISTEMA')")
    public ResponseEntity<ApiResponse<Integer>> guardar(@Valid @RequestBody ManualRequest request) {
        return ResponseEntity.ok(service.guardarManual(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MOD_MNUCONTROLDELSISTEMA')")
    public ResponseEntity<ApiResponse<Boolean>> eliminar(@PathVariable Integer id) {
        return ResponseEntity.ok(service.eliminarManual(id));
    }

    @PostMapping("/solicitar-url")
    @PreAuthorize("hasAuthority('MOD_MNUCONTROLDELSISTEMA')")
    public ResponseEntity<ApiResponse<RespuestaCargaDto>> solicitarUrlTemporal(
            @Valid @RequestBody SolicitarUrlRequest request) {
        return ResponseEntity.ok(service.solicitarUrlTemporal(request));
    }

    @PostMapping("/versiones")
    @PreAuthorize("hasAuthority('MOD_MNUCONTROLDELSISTEMA')")
    public ResponseEntity<ApiResponse<Integer>> publicarVersion(@Valid @RequestBody VersionRequest request) {
        return ResponseEntity.ok(service.publicarVersion(request));
    }

    @GetMapping("/{manualId}/versiones")
    public ResponseEntity<ApiResponse<List<VersionResponse>>> listarVersiones(@PathVariable Integer manualId) {
        return ResponseEntity.ok(service.listarVersiones(manualId));
    }

    // NUEVO: Endpoint para obtener la URL de descarga del archivo físico en AWS/MinIO
    @GetMapping("/{manualId}/versiones/{version}/descargar")
    public ResponseEntity<ApiResponse<String>> descargarArchivo(
            @PathVariable Integer manualId,
            @PathVariable Integer version) {
        return ResponseEntity.ok(service.obtenerUrlDescarga(manualId, version));
    }
}