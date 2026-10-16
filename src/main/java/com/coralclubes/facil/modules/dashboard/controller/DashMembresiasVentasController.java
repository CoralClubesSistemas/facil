package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashMembresiasVentasService;
import com.coralclubes.facil.modules.dashboard.dto.response.EstatusMembresiasResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.MejoresClientesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.MembresiasProcesablesDetallesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.MovimientoVentaXDesarrolloResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.UltimasMembresiasResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.UltimosClientesResponse;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard/admin/membresias/ventas")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashMembresiasVentasController {

    private final DashMembresiasVentasService dashMembresiasVentasService;


    @GetMapping("/membresias-desarrollos")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> spDashMembresiasDesarrollos(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashMembresiasVentasService.spDashMembresiasDesarrollos(fechaInicio, fechaFin)));
    }

    @GetMapping("/movimientos-ventas-x-desarrollo")
    public ResponseEntity<ApiResponse<List<MovimientoVentaXDesarrolloResponse>>> spDashMovimientosVentasXDesarrollo(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) String desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashMembresiasVentasService.spDashMovimientosVentasXDesarrollo(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/membresias-procesables")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> spDashMembresiasProcesables(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashMembresiasVentasService.spDashMembresiasProcesables(fechaInicio, fechaFin)));
    }

    @GetMapping("/membresias-procesables-detalles")
    public ResponseEntity<ApiResponse<List<MembresiasProcesablesDetallesResponse>>> spDashMembresiasProcesablesDetalles(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) String desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashMembresiasVentasService.spDashMembresiasProcesablesDetalles(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/mejores-clientes")
    public ResponseEntity<ApiResponse<List<MejoresClientesResponse>>> spDashMejoresClientes(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) Integer desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashMembresiasVentasService.spDashMejoresClientes(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/ultimos-clientes")
    public ResponseEntity<ApiResponse<List<UltimosClientesResponse>>> spDashUltimosClientes(@RequestParam(required = false) Integer desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashMembresiasVentasService.spDashUltimosClientes(desarrollo)));
    }

    @GetMapping("/ultimas-membresias")
    public ResponseEntity<ApiResponse<List<UltimasMembresiasResponse>>> spDashUltimasMembresias() {
        return ResponseEntity.ok(ApiResponse.success(dashMembresiasVentasService.spDashUltimasMembresias()));
    }

    @GetMapping("/estatus-membresias")
    public ResponseEntity<ApiResponse<List<EstatusMembresiasResponse>>> spDashEstatusMembresias(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashMembresiasVentasService.spDashEstatusMembresias(fechaInicio, fechaFin)));
    }
}
