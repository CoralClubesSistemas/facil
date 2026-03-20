package com.coralclubes.facil.modules.reservaciones.controller.admin;

import com.coralclubes.facil.modules.reservaciones.dto.request.CambiarEstatusTareaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.FinalizarTareaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.GuardarCamaristaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.CamaristaDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.InventarioBodegaDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.SugerenciaAmenidadDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.TareaDashboardDto;
import com.coralclubes.facil.modules.reservaciones.service.AmaDeLlavesService;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/housekeeping")
@RequiredArgsConstructor
public class AmaDeLlavesController {

    private final AmaDeLlavesService service;

    @GetMapping("/camaristas")
    @PreAuthorize("hasAuthority('MOD_SMNUHOUSEKEEPING')") // Ajusta el permiso a tu rol de Ama de Llaves
    public ResponseEntity<ApiResponse<List<CamaristaDto>>> obtenerCamaristas() {
        return ResponseEntity.ok(service.obtenerCamaristas());
    }

    @PostMapping("/camaristas/save")
    @PreAuthorize("hasAuthority('MOD_SMNUHOUSEKEEPING')")
    public ResponseEntity<ApiResponse<Boolean>> guardarCamarista(@Valid @RequestBody GuardarCamaristaRequest request) {
        return ResponseEntity.ok(service.guardarCamarista(request));
    }

    @DeleteMapping("/camaristas/{id}")
    @PreAuthorize("hasAuthority('MOD_SMNUHOUSEKEEPING')")
    public ResponseEntity<ApiResponse<Boolean>> desactivarCamarista(@PathVariable Integer id) {
        return ResponseEntity.ok(service.desactivarCamarista(id));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('MOD_SMNUHOUSEKEEPING')")
    public ResponseEntity<ApiResponse<List<TareaDashboardDto>>> obtenerDashboard() {
        return ResponseEntity.ok(service.obtenerTareasDashboard());
    }

    @PutMapping("/tareas/{idTarea}/estatus")
    @PreAuthorize("hasAuthority('MOD_SMNUHOUSEKEEPING')")
    public ResponseEntity<ApiResponse<Boolean>> cambiarEstatusTarea(
            @PathVariable Integer idTarea,
            @Valid @RequestBody CambiarEstatusTareaRequest request) {
        return ResponseEntity.ok(service.cambiarEstatusTarea(idTarea, request));
    }

    @GetMapping("/tareas/{idTarea}/sugerencias")
    @PreAuthorize("hasAuthority('MOD_SMNUHOUSEKEEPING')")
    public ResponseEntity<ApiResponse<List<SugerenciaAmenidadDto>>> obtenerSugerencias(@PathVariable Integer idTarea) {
        return ResponseEntity.ok(service.obtenerSugerencias(idTarea));
    }

    @GetMapping("/inventario")
    @PreAuthorize("hasAuthority('MOD_SMNUHOUSEKEEPING')")
    public ResponseEntity<ApiResponse<List<InventarioBodegaDto>>> obtenerInventario() {
        return ResponseEntity.ok(service.obtenerInventarioBodega());
    }

    @PostMapping("/tareas/{idTarea}/finalizar")
    @PreAuthorize("hasAuthority('MOD_SMNUHOUSEKEEPING')")
    public ResponseEntity<ApiResponse<Boolean>> finalizarTarea(
            @PathVariable Integer idTarea,
            @Valid @RequestBody FinalizarTareaRequest request) {
        return ResponseEntity.ok(service.finalizarTarea(idTarea, request));
    }
}
