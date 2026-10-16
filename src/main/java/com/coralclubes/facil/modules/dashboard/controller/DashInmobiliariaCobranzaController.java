package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashInmobiliariaCobranzaService;
import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaMovimientosCobranzaResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard/inmobiliaria/cobranza")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashInmobiliariaCobranzaController {

    private final DashInmobiliariaCobranzaService dashInmobiliariaCobranzaService;

    @GetMapping("/inmobiliaria-totales")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> spDashInmobiliariaTotales(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashInmobiliariaCobranzaService.spDashInmobiliariaTotales(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/inmobiliaria-clasificacion")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> spDashInmobiliariaClasificacion(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashInmobiliariaCobranzaService.spDashInmobiliariaClasificacion(fechaInicio, fechaFin)));
    }

    @GetMapping("/inmobiliaria-casas")
    public ResponseEntity<ApiResponse<List<InmobiliariaMovimientosCobranzaResponse>>> spDashInmobiliariaCasas(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashInmobiliariaCobranzaService.spDashInmobiliariaCasas(fechaInicio, fechaFin)));
    }

    @GetMapping("/inmobiliaria-terrenos")
    public ResponseEntity<ApiResponse<List<InmobiliariaMovimientosCobranzaResponse>>> spDashInmobiliariaTerrenos(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashInmobiliariaCobranzaService.spDashInmobiliariaTerrenos(fechaInicio, fechaFin)));
    }
}
