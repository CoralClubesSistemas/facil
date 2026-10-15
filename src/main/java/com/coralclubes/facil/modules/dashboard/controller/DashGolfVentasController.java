package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashGolfVentasService;
import com.coralclubes.facil.modules.dashboard.dto.response.DetallesMovimientosResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.GreenFeesPromocionesXFechaResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.GreenFeesXDiaResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard/golf/ventas")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_DASHBOARD')")
public class DashGolfVentasController {

    private final DashGolfVentasService dashGolfVentasService;

    @GetMapping("/golf-totales-ventas")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> spDashGolfTotalesVentas(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfVentasService.spDashGolfTotalesVentas(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/green-fees-ventas")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> spDashGreenFeesVentas(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfVentasService.spDashGreenFeesVentas(fechaInicio, fechaFin)));
    }

    @GetMapping("/green-fees-x-dia-ventas")
    public ResponseEntity<ApiResponse<List<GreenFeesXDiaResponse>>> spDashGreenFeesXDiaVentas(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfVentasService.spDashGreenFeesXDiaVentas(fechaInicio, fechaFin)));
    }

    @GetMapping("/green-fees-x-pte-ventas")
    public ResponseEntity<ApiResponse<List<GreenFeesXDiaResponse>>> spDashGreenFeesXPteVentas(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfVentasService.spDashGreenFeesXPteVentas(fechaInicio, fechaFin)));
    }

    @GetMapping("/green-fees-promociones")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> spDashGreenFeesPromociones(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfVentasService.spDashGreenFeesPromociones(fechaInicio, fechaFin)));
    }

    @GetMapping("/green-fees-promociones-detalles")
    public ResponseEntity<ApiResponse<List<DetallesMovimientosResponse>>> spDashGreenFeesPromocionesDetalles(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) String promocion) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfVentasService.spDashGreenFeesPromocionesDetalles(fechaInicio, fechaFin, promocion)));
    }

    @GetMapping("/green-fees-promociones-x-fecha")
    public ResponseEntity<ApiResponse<List<GreenFeesPromocionesXFechaResponse>>> spDashGreenFeesPromocionesXFecha(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfVentasService.spDashGreenFeesPromocionesXFecha(fechaInicio, fechaFin)));
    }

    @GetMapping("/torneos-periodo")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> spDashTorneosPeriodo(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfVentasService.spDashTorneosPeriodo(fechaInicio, fechaFin)));
    }

    @GetMapping("/torneos-totales")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> spDashTorneosTotales(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfVentasService.spDashTorneosTotales(fechaInicio, fechaFin).orElse(null)));
    }
}
