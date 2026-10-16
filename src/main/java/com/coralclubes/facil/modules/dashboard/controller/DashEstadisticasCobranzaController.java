package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashEstadisticasCobranzaService;
import com.coralclubes.facil.modules.dashboard.dto.response.EstadisticasTotalesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.IngresosTotalesResponse;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard/estadisticas/cobranza")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashEstadisticasCobranzaController {

    private final DashEstadisticasCobranzaService dashEstadisticasCobranzaService;

    @GetMapping("/ingresos-totales")
    public ResponseEntity<ApiResponse<IngresosTotalesResponse>> obtenerIngresosTotales(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasCobranzaService.obtenerIngresosTotales(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/estadisticas-totales")
    public ResponseEntity<ApiResponse<List<EstadisticasTotalesResponse>>> obtenerEstadisticasTotales(@RequestParam(required = false) Integer anio) {
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasCobranzaService.obtenerEstadisticasTotales(anio)));
    }

    @GetMapping("/estadisticas-totales-x-desarrollo")
    public ResponseEntity<ApiResponse<List<EstadisticasTotalesResponse>>> obtenerEstadisticasTotalesXDesarrollo(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) String desarrollo
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasCobranzaService.obtenerEstadisticasTotalesXDesarrollo(anio, desarrollo)));
    }
}
