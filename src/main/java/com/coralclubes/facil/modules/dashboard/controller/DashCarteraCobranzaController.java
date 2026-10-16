package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashCarteraCobranzaService;
import com.coralclubes.facil.modules.dashboard.dto.response.InventarioMembresiasResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard/cartera/cobranza")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashCarteraCobranzaController {

    private final DashCarteraCobranzaService dashCarteraCobranzaService;

    @GetMapping("/inventario-membresias-totales")
    public ResponseEntity<ApiResponse<List<InventarioMembresiasResponse>>> spObtieneInventarioMembresiasTotales(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashCarteraCobranzaService.spObtieneInventarioMembresiasTotales(fechaInicio, fechaFin)));
    }

    @GetMapping("/inventario-membresias-desarrollo")
    public ResponseEntity<ApiResponse<List<InventarioMembresiasResponse>>> spObtieneInventarioMembresiasDesarrollo(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) String desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashCarteraCobranzaService.spObtieneInventarioMembresiasDesarrollo(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/membresias-totales-global")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> spInventarioMembresiasTotalesGlobal(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashCarteraCobranzaService.spInventarioMembresiasTotalesGlobal(fechaInicio, fechaFin)));
    }

    @GetMapping("/inventario-membresias-totales-x-cartera")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> spObtieneInventarioMembresiasTotalesXCartera(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) String desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashCarteraCobranzaService.spObtieneInventarioMembresiasTotalesXCartera(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/inventario-membresias-totales-x-tipo")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> spObtieneInventarioMembresiasTotalesXTipo(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) String desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashCarteraCobranzaService.spObtieneInventarioMembresiasTotalesXTipo(fechaInicio, fechaFin, desarrollo)));
    }
}
