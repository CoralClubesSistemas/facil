package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashGolfCobranzaService;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard/golf/cobranza")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashGolfCobranzaController {

    private final DashGolfCobranzaService dashGolfCobranzaService;

    @GetMapping("/golf-totales")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> spDashGolfTotales(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfCobranzaService.spDashGolfTotales(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/green-fees-totales")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> spDashGreenFeesTotales(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfCobranzaService.spDashGreenFeesTotales(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/green-fees-clasificacion")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> spDashGreenFeesClasificacion(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfCobranzaService.spDashGreenFeesClasificacion(fechaInicio, fechaFin)));
    }

    @GetMapping("/otros-ingresos-golf")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> spDashOtrosIngresosGolf(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfCobranzaService.spDashOtrosIngresosGolf(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/otros-ingresos-golf-detalles")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> spDashOtrosIngresosGolfDetalles(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashGolfCobranzaService.spDashOtrosIngresosGolfDetalles(fechaInicio, fechaFin)));
    }
}
