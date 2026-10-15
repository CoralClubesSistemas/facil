package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashMembresiasCobranzaService;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard/membresias/cobranza")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_DASHBOARD')")
public class DashMembresiasCobranzaController {

    private final DashMembresiasCobranzaService dashMembresiasCobranzaService;

    @GetMapping("/membresias-totales")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> spDashMembresiasTotales(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashMembresiasCobranzaService.spDashMembresiasTotales(fechaInicio, fechaFin).orElse(null)));
    }
}
