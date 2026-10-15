package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashClasificacionesCobranzaService;
import com.coralclubes.facil.modules.dashboard.dto.response.ClasificacionesDetallesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.ClasificacionesResponse;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard/clasificaciones/cobranza")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_DASHBOARD')")
public class DashClasificacionesCobranzaController {

    private final DashClasificacionesCobranzaService dashClasificacionesCobranzaService;

    @GetMapping("/totales-clasificaciones")
    public ResponseEntity<ApiResponse<List<ClasificacionesResponse>>> spDashTotalesClasificaciones(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) String desarrollos, @RequestParam(required = false) String clasificaciones) {
        return ResponseEntity.ok(ApiResponse.success(dashClasificacionesCobranzaService.spDashTotalesClasificaciones(fechaInicio, fechaFin, desarrollos, clasificaciones)));
    }

    @GetMapping("/clasificaciones-detalles")
    public ResponseEntity<ApiResponse<List<ClasificacionesDetallesResponse>>> spDashClasificacionesDetalles(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) String desarrollos, @RequestParam(required = false) String clasificaciones) {
        return ResponseEntity.ok(ApiResponse.success(dashClasificacionesCobranzaService.spDashClasificacionesDetalles(fechaInicio, fechaFin, desarrollos, clasificaciones)));
    }
}
