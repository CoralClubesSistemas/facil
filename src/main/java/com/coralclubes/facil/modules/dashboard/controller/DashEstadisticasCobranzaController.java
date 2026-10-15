package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashEstadisticasCobranzaService;
import com.coralclubes.facil.modules.dashboard.dto.response.EstadisticasTotalesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.IngresosTotalesResponse;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard/estadisticas/cobranza")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_DASHBOARD')")
public class DashEstadisticasCobranzaController {

    private final DashEstadisticasCobranzaService dashEstadisticasCobranzaService;

    @GetMapping("/ingresos-totales")
    public ResponseEntity<ApiResponse<IngresosTotalesResponse>> spDashBoardIngresosTotales(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasCobranzaService.spDashBoardIngresosTotales(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/estadisticas-totales")
    public ResponseEntity<ApiResponse<List<EstadisticasTotalesResponse>>> spDashEstadisticasTotales(@RequestParam(required = false) Integer anio) {
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasCobranzaService.spDashEstadisticasTotales(anio)));
    }

    @GetMapping("/estadisticas-totales-x-desarrollo")
    public ResponseEntity<ApiResponse<List<EstadisticasTotalesResponse>>> spDashEstadisticasTotalesXDesarrollo(@RequestParam(required = false) Integer anio, @RequestParam(required = false) String desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasCobranzaService.spDashEstadisticasTotalesXDesarrollo(anio, desarrollo)));
    }
}
