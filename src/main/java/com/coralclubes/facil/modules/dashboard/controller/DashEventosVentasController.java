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

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard/eventos/ventas")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashEventosVentasController {

    private final DashEventosVentasService dashEventosVentasService;


    @GetMapping("/eventos-desarrollos-ventas")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> spDashEventosDesarrollosVentas(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosVentasService.spDashEventosDesarrollosVentas(fechaInicio, fechaFin)));
    }

    @GetMapping("/eventos-detalles-ventas")
    public ResponseEntity<ApiResponse<List<EventosDetallesResponse>>> spDashEventosDetallesVentas(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) Integer desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosVentasService.spDashEventosDetallesVentas(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/eventos-vendidos")
    public ResponseEntity<ApiResponse<List<EventosOperadosResponse>>> spDashEventosVendidos(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) String desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosVentasService.spDashEventosVendidos(fechaInicio, fechaFin, desarrollo)));
    }
}
