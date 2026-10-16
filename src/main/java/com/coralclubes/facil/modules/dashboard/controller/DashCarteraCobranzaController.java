package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashCarteraCobranzaService;
import com.coralclubes.facil.modules.dashboard.dto.response.InventarioMembresiasResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard/cartera/cobranza")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashCarteraCobranzaController {

    private final DashCarteraCobranzaService dashCarteraCobranzaService;

    @GetMapping("/inventario-membresias-totales")
    public ResponseEntity<ApiResponse<List<InventarioMembresiasResponse>>> obtenerInventarioMembresiasTotales(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashCarteraCobranzaService.obtenerInventarioMembresiasTotales(fechaInicio, fechaFin)));
    }

    @GetMapping("/inventario-membresias-desarrollo")
    public ResponseEntity<ApiResponse<List<InventarioMembresiasResponse>>> obtenerInventarioMembresiasDesarrollo(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) String desarrollo
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashCarteraCobranzaService.obtenerInventarioMembresiasDesarrollo(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/membresias-totales-global")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> obtenerInventarioMembresiasTotalesGlobal(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashCarteraCobranzaService.obtenerInventarioMembresiasTotalesGlobal(fechaInicio, fechaFin)));
    }

    @GetMapping("/inventario-membresias-totales-x-cartera")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> obtenerInventarioMembresiasTotalesXCartera(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) String desarrollo
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashCarteraCobranzaService.obtenerInventarioMembresiasTotalesXCartera(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/inventario-membresias-totales-x-tipo")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> obtenerInventarioMembresiasTotalesXTipo(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) String desarrollo
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashCarteraCobranzaService.obtenerInventarioMembresiasTotalesXTipo(fechaInicio, fechaFin, desarrollo)));
    }
}
