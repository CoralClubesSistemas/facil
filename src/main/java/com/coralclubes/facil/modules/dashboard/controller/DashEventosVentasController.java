package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashEventosVentasService;
import com.coralclubes.facil.modules.dashboard.dto.response.EventosDetallesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.EventosOperadosResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard/eventos/ventas")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashEventosVentasController {

    private final DashEventosVentasService dashEventosVentasService;

    @GetMapping("/eventos-desarrollos-ventas")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> obtenerEventosDesarrollosVentas(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosVentasService.obtenerEventosDesarrollosVentas(fechaInicio, fechaFin)));
    }

    @GetMapping("/eventos-detalles-ventas")
    public ResponseEntity<ApiResponse<List<EventosDetallesResponse>>> obtenerEventosDetallesVentas(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) Integer desarrollo
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosVentasService.obtenerEventosDetallesVentas(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/eventos-vendidos")
    public ResponseEntity<ApiResponse<List<EventosOperadosResponse>>> obtenerEventosVendidos(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) String desarrollo
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosVentasService.obtenerEventosVendidos(fechaInicio, fechaFin, desarrollo)));
    }
}
