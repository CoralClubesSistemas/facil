package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashInmobiliariaVentasService;
import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaMovimientosResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard/inmobiliaria/ventas")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_DASHBOARD')")
public class DashInmobiliariaVentasController {

    private final DashInmobiliariaVentasService dashInmobiliariaVentasService;

    @GetMapping("/inmobiliaria-totales-ventas")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> spDashInmobiliariaTotalesVentas(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashInmobiliariaVentasService.spDashInmobiliariaTotalesVentas(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/inmobiliaria-clasificacion-ventas")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> spDashInmobiliariaClasificacionVentas(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashInmobiliariaVentasService.spDashInmobiliariaClasificacionVentas(fechaInicio, fechaFin)));
    }

    @GetMapping("/inmobiliaria-casas-ventas")
    public ResponseEntity<ApiResponse<List<InmobiliariaMovimientosResponse>>> spDashInmobiliariaCasasVentas(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashInmobiliariaVentasService.spDashInmobiliariaCasasVentas(fechaInicio, fechaFin)));
    }

    @GetMapping("/inmobiliaria-terrenos-ventas")
    public ResponseEntity<ApiResponse<List<InmobiliariaMovimientosResponse>>> spDashInmobiliariaTerrenosVentas(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashInmobiliariaVentasService.spDashInmobiliariaTerrenosVentas(fechaInicio, fechaFin)));
    }
}
