package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashGolfCobranzaService;
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
@RequestMapping("/api/v1/admin/dashboard/golf/cobranza")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashGolfCobranzaController {

    private final DashGolfCobranzaService dashGolfCobranzaService;

    @GetMapping("/golf-totales")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> obtenerGolfTotales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfCobranzaService.obtenerGolfTotales(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/green-fees-totales")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> obtenerGreenFeesTotales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfCobranzaService.obtenerGreenFeesTotales(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/green-fees-clasificacion")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> obtenerGreenFeesClasificacion(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfCobranzaService.obtenerGreenFeesClasificacion(fechaInicio, fechaFin)));
    }

    @GetMapping("/otros-ingresos-golf")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> obtenerOtrosIngresosGolf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfCobranzaService.obtenerOtrosIngresosGolf(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/otros-ingresos-golf-detalles")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> obtenerOtrosIngresosGolfDetalles(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfCobranzaService.obtenerOtrosIngresosGolfDetalles(fechaInicio, fechaFin)));
    }
}
