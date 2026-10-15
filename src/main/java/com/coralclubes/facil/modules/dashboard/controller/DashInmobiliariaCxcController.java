package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashInmobiliariaCxcService;
import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaXCobrarDetalleClienteResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaXCobrarDetallesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.InmobiliariaXCobrarTotalesResponse;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard/inmobiliaria/cxc")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_DASHBOARD')")
public class DashInmobiliariaCxcController {

    private final DashInmobiliariaCxcService dashInmobiliariaCxcService;

    @GetMapping("/inmobiliaria-x-cobrar-totales")
    public ResponseEntity<ApiResponse<List<InmobiliariaXCobrarTotalesResponse>>> spDashInmobiliariaXCobrarTotales(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashInmobiliariaCxcService.spDashInmobiliariaXCobrarTotales(fechaInicio, fechaFin)));
    }

    @GetMapping("/inmobiliaria-x-cobrar-detalles")
    public ResponseEntity<ApiResponse<List<InmobiliariaXCobrarDetallesResponse>>> spDashInmobiliariaXCobrarDetalles(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) String clasificacion) {
        return ResponseEntity.ok(ApiResponse.success(dashInmobiliariaCxcService.spDashInmobiliariaXCobrarDetalles(fechaInicio, fechaFin, clasificacion)));
    }

    @GetMapping("/inmobiliaria-x-cobrar-detalles-x-cliente")
    public ResponseEntity<ApiResponse<List<InmobiliariaXCobrarDetalleClienteResponse>>> spDashInmobiliariaXCobrarDetallesXCliente(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) String membresia) {
        return ResponseEntity.ok(ApiResponse.success(dashInmobiliariaCxcService.spDashInmobiliariaXCobrarDetallesXCliente(fechaInicio, fechaFin, membresia)));
    }
}
