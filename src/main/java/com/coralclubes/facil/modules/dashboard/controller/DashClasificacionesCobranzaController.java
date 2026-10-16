package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashClasificacionesCobranzaService;
import com.coralclubes.facil.modules.dashboard.dto.response.ClasificacionesDetallesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.ClasificacionesResponse;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard/clasificaciones/cobranza")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashClasificacionesCobranzaController {

    private final DashClasificacionesCobranzaService dashClasificacionesCobranzaService;

    @GetMapping("/totales-clasificaciones")
    public ResponseEntity<ApiResponse<List<ClasificacionesResponse>>> obtenerTotalesClasificaciones(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String desarrollos,
            @RequestParam(required = false) String clasificaciones
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashClasificacionesCobranzaService.obtenerTotalesClasificaciones(fechaInicio, fechaFin, desarrollos, clasificaciones)));
    }

    @GetMapping("/clasificaciones-detalles")
    public ResponseEntity<ApiResponse<List<ClasificacionesDetallesResponse>>> obtenerClasificacionesDetalles(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String desarrollos,
            @RequestParam(required = false) String clasificaciones
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashClasificacionesCobranzaService.obtenerClasificacionesDetalles(fechaInicio, fechaFin, desarrollos, clasificaciones)));
    }
}
