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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard/admin/membresias/ventas")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashMembresiasVentasController {

    private final DashMembresiasVentasService dashMembresiasVentasService;

    @GetMapping("/membresias-desarrollos")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> obtenerMembresiasDesarrollos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashMembresiasVentasService.obtenerMembresiasDesarrollos(fechaInicio, fechaFin)));
    }

    @GetMapping("/movimientos-ventas-x-desarrollo")
    public ResponseEntity<ApiResponse<List<MovimientoVentaXDesarrolloResponse>>> obtenerMovimientosVentasXDesarrollo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashMembresiasVentasService.obtenerMovimientosVentasXDesarrollo(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/membresias-procesables")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> obtenerMembresiasProcesables(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashMembresiasVentasService.obtenerMembresiasProcesables(fechaInicio, fechaFin)));
    }

    @GetMapping("/membresias-procesables-detalles")
    public ResponseEntity<ApiResponse<List<MembresiasProcesablesDetallesResponse>>> obtenerMembresiasProcesablesDetalles(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashMembresiasVentasService.obtenerMembresiasProcesablesDetalles(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/mejores-clientes")
    public ResponseEntity<ApiResponse<List<MejoresClientesResponse>>> obtenerMejoresClientes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Integer desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashMembresiasVentasService.obtenerMejoresClientes(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/ultimos-clientes")
    public ResponseEntity<ApiResponse<List<UltimosClientesResponse>>> obtenerUltimosClientes(@RequestParam(required = false) Integer desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashMembresiasVentasService.obtenerUltimosClientes(desarrollo)));
    }

    @GetMapping("/ultimas-membresias")
    public ResponseEntity<ApiResponse<List<UltimasMembresiasResponse>>> obtenerUltimasMembresias() {
        return ResponseEntity.ok(ApiResponse.success(dashMembresiasVentasService.obtenerUltimasMembresias()));
    }

    @GetMapping("/estatus-membresias")
    public ResponseEntity<ApiResponse<List<EstatusMembresiasResponse>>> obtenerEstatusMembresias(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashMembresiasVentasService.obtenerEstatusMembresias(fechaInicio, fechaFin)));
    }
}
