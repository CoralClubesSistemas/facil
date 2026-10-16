package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.dto.response.EstadisticasTotalesXDesarrolloResponse;
import com.coralclubes.facil.modules.dashboard.service.DashEstadisticasVentasService;
import com.coralclubes.facil.modules.dashboard.dto.response.EstadisticasTotalesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.IngresosTotalesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard/estadisticas/ventas")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashEstadisticasVentasController {

    private final DashEstadisticasVentasService dashEstadisticasVentasService;
    private final UserContext userContext;

    @GetMapping("/membresias-totales")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> obtenerMembresiasTotalesVentas(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasVentasService.obtenerMembresiasTotalesVentas(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/golf-totales")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> obtenerGolfTotalesVentas(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasVentasService.obtenerGolfTotalesVentas(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/eventos-totales")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> obtenerEventosTotalesVentas(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasVentasService.obtenerEventosTotalesVentas(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/inmobiliaria-totales")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> obtenerInmobiliariaTotalesVentas(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasVentasService.obtenerInmobiliariaTotalesVentas(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/ingresos-totales")
    public ResponseEntity<ApiResponse<IngresosTotalesResponse>> obtenerIngresosTotalesVentas(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin
    ) {
        Integer desarrollo = userContext.getIdDesarrollo();
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasVentasService.obtenerIngresosTotalesVentas(fechaInicio, fechaFin, desarrollo).orElse(null)));
    }

    @GetMapping("/estadisticas-totales")
    public ResponseEntity<ApiResponse<List<EstadisticasTotalesResponse>>> obtenerEstadisticasTotalesVentas(@RequestParam(required = false) Integer anio) {
        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasVentasService.obtenerEstadisticasTotalesVentas(anio)));
    }

    @GetMapping("/estadisticas-totales-x-desarrollo")
    public ResponseEntity<ApiResponse<List<EstadisticasTotalesXDesarrolloResponse>>> obtenerEstadisticasTotalesXDesarrolloVentas(
            @RequestParam(required = false) Integer anio
    ) {
        Integer desarrollo = userContext.getIdDesarrollo();

        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasVentasService.obtenerEstadisticasTotalesXDesarrolloVentas(anio, desarrollo)));
    }

    @GetMapping("/estadisticas-totales-crecimiento-mensual")
    public ResponseEntity<ApiResponse<List<EstadisticasTotalesXDesarrolloResponse>>> obtenerCrecimientoMensualXDesarrolloVentas(
            @RequestParam(required = false) Integer anio
    ) {
        Integer desarrollo = userContext.getIdDesarrollo();

        return ResponseEntity.ok(ApiResponse.success(dashEstadisticasVentasService.obtenerCrecimientoMensualXDesarrolloVentas(anio, desarrollo)));
    }
}
