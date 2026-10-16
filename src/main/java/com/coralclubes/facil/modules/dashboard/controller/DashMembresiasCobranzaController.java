package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashMembresiasCobranzaService;
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
@RequestMapping("/api/v1/admin/dashboard/membresias/cobranza")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashMembresiasCobranzaController {

    private final DashMembresiasCobranzaService dashMembresiasCobranzaService;

    @GetMapping("/membresias-totales")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> obtenerMembresiasTotales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashMembresiasCobranzaService.obtenerMembresiasTotales(fechaInicio, fechaFin).orElse(null)));
    }
}
