package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashGolfVentasService;
import com.coralclubes.facil.modules.dashboard.dto.response.DetallesMovimientosResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.GreenFeesPromocionesXFechaResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.GreenFeesXDiaResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard/golf/ventas")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashGolfVentasController {

    private final DashGolfVentasService dashGolfVentasService;

    @GetMapping("/green-fees-ventas")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> obtenerGreenFeesVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfVentasService.obtenerGreenFeesVentas(fechaInicio, fechaFin)));
    }

    @GetMapping("/green-fees-x-dia-ventas")
    public ResponseEntity<ApiResponse<List<GreenFeesXDiaResponse>>> obtenerGreenFeesXDiaVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfVentasService.obtenerGreenFeesXDiaVentas(fechaInicio, fechaFin)));
    }

    @GetMapping("/green-fees-x-pte-ventas")
    public ResponseEntity<ApiResponse<List<GreenFeesXDiaResponse>>> obtenerGreenFeesXPteVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfVentasService.obtenerGreenFeesXPteVentas(fechaInicio, fechaFin)));
    }

    @GetMapping("/green-fees-promociones")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> obtenerGreenFeesPromociones(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfVentasService.obtenerGreenFeesPromociones(fechaInicio, fechaFin)));
    }

    @GetMapping("/green-fees-promociones-detalles")
    public ResponseEntity<ApiResponse<List<DetallesMovimientosResponse>>> obtenerGreenFeesPromocionesDetalles(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String promocion
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfVentasService.obtenerGreenFeesPromocionesDetalles(fechaInicio, fechaFin, promocion)));
    }

    @GetMapping("/green-fees-promociones-x-fecha")
    public ResponseEntity<ApiResponse<List<GreenFeesPromocionesXFechaResponse>>> obtenerGreenFeesPromocionesXFecha(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfVentasService.obtenerGreenFeesPromocionesXFecha(fechaInicio, fechaFin)));
    }

    @GetMapping("/torneos-periodo")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> obtenerTorneosPeriodo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfVentasService.obtenerTorneosPeriodo(fechaInicio, fechaFin)));
    }

    @GetMapping("/torneos-totales")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> obtenerTorneosTotales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfVentasService.obtenerTorneosTotales(fechaInicio, fechaFin).orElse(null)));
    }
}
