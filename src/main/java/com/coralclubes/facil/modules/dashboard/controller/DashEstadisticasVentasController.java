package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashEstadisticasVentasService;
import com.coralclubes.facil.modules.dashboard.dto.response.EstadisticasTotalesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.IngresosTotalesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard/estadisticas/ventas")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashEstadisticasVentasController {

    private final DashEstadisticasVentasService dashEstadisticasVentasService;

    @GetMapping("/membresias-totales-ventas")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> spDashMembresiasTotalesVentas(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasVentasService.spDashMembresiasTotalesVentas(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/golf-totales-ventas")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> spDashGolfTotalesVentas(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasVentasService.spDashGolfTotalesVentas(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/eventos-totales-ventas")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> spDashEventosTotalesVentas(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasVentasService.spDashEventosTotalesVentas(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/inmobiliaria-totales-ventas")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> spDashInmobiliariaTotalesVentas(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasVentasService.spDashInmobiliariaTotalesVentas(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/ingresos-totales-ventas")
    public ResponseEntity<ApiResponse<IngresosTotalesResponse>> spDashBoardIngresosTotalesVentas(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasVentasService.spDashBoardIngresosTotalesVentas(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/estadisticas-totales-ventas")
    public ResponseEntity<ApiResponse<List<EstadisticasTotalesResponse>>> spDashEstadisticasTotalesVentas(@RequestParam(required = false) Integer anio) {
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasVentasService.spDashEstadisticasTotalesVentas(anio)));
    }

    @GetMapping("/estadisticas-totales-x-desarrollo-ventas")
    public ResponseEntity<ApiResponse<List<EstadisticasTotalesResponse>>> spDashEstadisticasTotalesXDesarrolloVentas(@RequestParam(required = false) Integer anio, @RequestParam(required = false) String desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasVentasService.spDashEstadisticasTotalesXDesarrolloVentas(anio, desarrollo)));
    }
}
