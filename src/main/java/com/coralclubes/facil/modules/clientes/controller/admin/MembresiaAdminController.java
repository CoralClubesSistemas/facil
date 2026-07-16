package com.coralclubes.facil.modules.clientes.controller.admin;

import com.coralclubes.facil.modules.clientes.dto.response.*;
import com.coralclubes.facil.modules.clientes.service.MembresiaService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/clientes/membresias")
@RequiredArgsConstructor
public class MembresiaAdminController {

    private final MembresiaService service;

    @GetMapping("/{membresia}/cancelacion")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MembresiaCancelacionDto>> obtenerDatosCancelacion(
            @PathVariable String membresia
    ) {
        MembresiaCancelacionDto datos = service.obtenerDatosCancelacion(membresia)
                .orElse(null);
        return ResponseEntity.ok(ApiResponse.success("Datos de cancelación obtenidos exitosamente.", datos));
    }

    @GetMapping("/{membresia}/cargo-automatico")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MembresiaAfiliacionDto>> obtenerAfiliacionCargoAutomatico(
            @PathVariable String membresia
    ) {
        MembresiaAfiliacionDto datos = service.obtenerAfiliacionCargoAutomatico(membresia)
                .orElse(null);
        return ResponseEntity.ok(ApiResponse.success("Datos de cargo automático obtenidos exitosamente.", datos));
    }

    @GetMapping("/{membresia}/vigencia")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MembresiaVigenciaDto>> obtenerVigencia(
            @PathVariable String membresia
    ) {
        MembresiaVigenciaDto datos = service.obtenerVigencia(membresia)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró información de vigencia para la membresía: " + membresia));
        return ResponseEntity.ok(ApiResponse.success("Datos de vigencia obtenidos exitosamente.", datos));
    }

    @GetMapping("/{membresia}/accesos-fin-semana")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MembresiaAccesosFinSemanaDto>> obtenerAccesosFinDeSemana(
            @PathVariable String membresia
    ) {
        MembresiaAccesosFinSemanaDto datos = service.obtenerAccesosFinDeSemana(membresia)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró información de accesos para la membresía: " + membresia));
        return ResponseEntity.ok(ApiResponse.success("Datos de accesos de fin de semana obtenidos exitosamente.", datos));
    }

    @GetMapping("/{membresia}/plan-venta")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MembresiaDetallesPlanVentaDto>> obtenerDetallesPlanVenta(
            @PathVariable String membresia,
            @RequestParam(required = false) Integer plan
    ) {
        MembresiaDetallesPlanVentaDto datos = service.obtenerDetallesPlanVenta(membresia, plan)
                .orElseThrow(() -> new IllegalArgumentException("No se encontraron detalles del plan de venta para la membresía: " + membresia));
        return ResponseEntity.ok(ApiResponse.success("Detalles del plan de venta obtenidos exitosamente.", datos));
    }

    @GetMapping("/{membresia}/procesable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MembresiaDetalleProcesableDto>> obtenerDetalleProcesable(
            @PathVariable String membresia
    ) {
        MembresiaDetalleProcesableDto datos = service.obtenerDetalleProcesable(membresia).orElse(null);
        return ResponseEntity.ok(ApiResponse.success("Detalle procesable obtenido exitosamente.", datos));
    }
}
