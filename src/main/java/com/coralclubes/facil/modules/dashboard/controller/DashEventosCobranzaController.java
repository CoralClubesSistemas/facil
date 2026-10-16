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

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard/eventos/cobranza")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashEventosCobranzaController {

    private final DashEventosCobranzaService dashEventosCobranzaService;

    @GetMapping("/eventos-totales")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> spDashEventosTotales(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.spDashEventosTotales(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/eventos-desarrollos")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> spDashEventosDesarrollos(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.spDashEventosDesarrollos(fechaInicio, fechaFin)));
    }

    @GetMapping("/eventos-detalles")
    public ResponseEntity<ApiResponse<List<EventosDetallesResponse>>> spDashEventosDetalles(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) Integer desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.spDashEventosDetalles(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/eventos-amortizados")
    public ResponseEntity<ApiResponse<List<EventosOperadosResponse>>> spDashEventosAmortizados(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) String desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.spDashEventosAmortizados(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/eventos-anticipos")
    public ResponseEntity<ApiResponse<List<EventosDetallesResponse>>> spDashEventosAnticipos(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) Integer desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.spDashEventosAnticipos(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/eventos-anticipos-total")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> spDashEventosAnticiposTotal(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) Integer desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.spDashEventosAnticiposTotal(fechaInicio, fechaFin, desarrollo).orElse(null)));
    }

    @GetMapping("/eventos-liquidacion")
    public ResponseEntity<ApiResponse<List<EventosDetallesResponse>>> spDashEventosLiquidacion(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) Integer desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.spDashEventosLiquidacion(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/eventos-liquidacion-total")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> spDashEventosLiquidacionTotal(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) Integer desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.spDashEventosLiquidacionTotal(fechaInicio, fechaFin, desarrollo).orElse(null)));
    }

    @GetMapping("/eventos-operados")
    public ResponseEntity<ApiResponse<List<EventosOperadosResponse>>> spDashEventosOperados(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) Integer desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.spDashEventosOperados(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/obtener-adendas-eventos")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> spDashObtenerAdendasEventos(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.spDashObtenerAdendasEventos(fechaInicio, fechaFin)));
    }

    @GetMapping("/obtener-adendas-eventos-detalles")
    public ResponseEntity<ApiResponse<List<AdendasDetallesResponse>>> spDashObtenerAdendasEventosDetalles(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) Integer desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashEventosCobranzaService.spDashObtenerAdendasEventosDetalles(fechaInicio, fechaFin, desarrollo)));
    }
}
