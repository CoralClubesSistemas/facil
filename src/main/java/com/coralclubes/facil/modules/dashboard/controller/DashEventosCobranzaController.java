package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashEventosCobranzaService;
import com.coralclubes.facil.modules.dashboard.dto.response.AdendasDetallesResponse;
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
@RequestMapping("/api/v1/admin/dashboard/eventos/cobranza")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashEventosCobranzaController {

    private final DashEventosCobranzaService dashEventosCobranzaService;

    @GetMapping("/eventos-totales")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> obtenerEventosTotales(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.obtenerEventosTotales(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/eventos-desarrollos")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> obtenerEventosDesarrollos(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.obtenerEventosDesarrollos(fechaInicio, fechaFin)));
    }

    @GetMapping("/eventos-detalles")
    public ResponseEntity<ApiResponse<List<EventosDetallesResponse>>> obtenerEventosDetalles(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) Integer desarrollo
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.obtenerEventosDetalles(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/eventos-amortizados")
    public ResponseEntity<ApiResponse<List<EventosOperadosResponse>>> obtenerEventosAmortizados(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) String desarrollo
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.obtenerEventosAmortizados(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/eventos-anticipos")
    public ResponseEntity<ApiResponse<List<EventosDetallesResponse>>> obtenerEventosAnticipos(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) Integer desarrollo
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.obtenerEventosAnticipos(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/eventos-anticipos-total")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> obtenerEventosAnticiposTotal(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) Integer desarrollo
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.obtenerEventosAnticiposTotal(fechaInicio, fechaFin, desarrollo).orElse(null)));
    }

    @GetMapping("/eventos-liquidacion")
    public ResponseEntity<ApiResponse<List<EventosDetallesResponse>>> obtenerEventosLiquidacion(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) Integer desarrollo
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.obtenerEventosLiquidacion(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/eventos-liquidacion-total")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> obtenerEventosLiquidacionTotal(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) Integer desarrollo
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.obtenerEventosLiquidacionTotal(fechaInicio, fechaFin, desarrollo).orElse(null)));
    }

    @GetMapping("/eventos-operados")
    public ResponseEntity<ApiResponse<List<EventosOperadosResponse>>> obtenerEventosOperados(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) Integer desarrollo
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.obtenerEventosOperados(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/obtener-adendas-eventos")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> obtenerAdendasEventos(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.obtenerAdendasEventos(fechaInicio, fechaFin)));
    }

    @GetMapping("/obtener-adendas-eventos-detalles")
    public ResponseEntity<ApiResponse<List<AdendasDetallesResponse>>> obtenerAdendasEventosDetalles(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) Integer desarrollo
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.obtenerAdendasEventosDetalles(fechaInicio, fechaFin, desarrollo)));
    }
}
