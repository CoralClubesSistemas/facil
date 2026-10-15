package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashEstadisticasVentasService;
import com.coralclubes.facil.modules.dashboard.dto.response.DetallesMovimientosResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.EstadisticasTotalesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.IngresosTotalesResponse;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard/estadisticas/ventas")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_DASHBOARD')")
public class DashEstadisticasVentasController {

    private final DashEstadisticasVentasService dashEstadisticasVentasService;

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

    @GetMapping("/detalles-movimientos")
    public ResponseEntity<ApiResponse<List<DetallesMovimientosResponse>>> spDashDetallesMovimientos(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) String desarrollo, @RequestParam(required = false) String movimientos) {
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasVentasService.spDashDetallesMovimientos(fechaInicio, fechaFin, desarrollo, movimientos)));
    }
}
