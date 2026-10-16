package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashInmobiliariaVentasService;
import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaMovimientosResponse;
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
@RequestMapping("/api/v1/admin/dashboard/inmobiliaria/ventas")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashInmobiliariaVentasController {

    private final DashInmobiliariaVentasService dashInmobiliariaVentasService;

    @GetMapping("/inmobiliaria-clasificacion-ventas")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> obtenerInmobiliariaClasificacionVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashInmobiliariaVentasService.obtenerInmobiliariaClasificacionVentas(fechaInicio, fechaFin)));
    }

    @GetMapping("/inmobiliaria-casas-ventas")
    public ResponseEntity<ApiResponse<List<InmobiliariaMovimientosResponse>>> obtenerInmobiliariaCasasVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashInmobiliariaVentasService.obtenerInmobiliariaCasasVentas(fechaInicio, fechaFin)));
    }

    @GetMapping("/inmobiliaria-terrenos-ventas")
    public ResponseEntity<ApiResponse<List<InmobiliariaMovimientosResponse>>> obtenerInmobiliariaTerrenosVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashInmobiliariaVentasService.obtenerInmobiliariaTerrenosVentas(fechaInicio, fechaFin)));
    }
}
