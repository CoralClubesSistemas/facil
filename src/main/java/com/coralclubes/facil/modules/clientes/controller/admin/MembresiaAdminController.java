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

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
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

    @GetMapping("/{membresia}/temporales")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MembresiaTemporalDto>>> obtenerMembresiasTemporales(
            @PathVariable String membresia
    ) {
        List<MembresiaTemporalDto> temporales = service.obtenerMembresiasTemporales(membresia);
        return ResponseEntity.ok(ApiResponse.success("Membresías temporales obtenidas exitosamente.", temporales));
    }

    @GetMapping("/{membresia}/accesos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MembresiaAccesoDto>>> obtenerAccesos(
            @PathVariable String membresia,
            @RequestParam(required = false) String desarrollo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false, defaultValue = "false") Boolean soloFS,
            @RequestParam(required = false, defaultValue = "1") Integer numeroPagina,
            @RequestParam(required = false, defaultValue = "20") Integer registrosPorPagina
    ) {
        List<MembresiaAccesoDto> accesos = service.obtenerAccesos(
                membresia,
                desarrollo,
                fechaDesde,
                fechaHasta,
                soloFS,
                numeroPagina,
                registrosPorPagina
        );
        return ResponseEntity.ok(ApiResponse.success("Accesos de la membresía obtenidos exitosamente.", accesos));
    }

    @GetMapping("/{membresia}/accesos/entradas-salidas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MembresiaAccesoEntradaSalidaDto>>> obtenerAccesosEntradasSalidas(
            @PathVariable String membresia,
            @RequestParam Integer desarrollo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaAccesoDesde,
            @RequestParam Integer beneficiario
    ) {
        List<MembresiaAccesoEntradaSalidaDto> accesos = service.obtenerAccesosEntradasSalidas(
                membresia,
                desarrollo,
                fechaAccesoDesde,
                beneficiario
        );
        return ResponseEntity.ok(ApiResponse.success("Entradas y salidas de acceso obtenidas exitosamente.", accesos));
    }

    @GetMapping("/{membresia}/referidos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MembresiaReferidoDto>>> obtenerReferidos(
            @PathVariable String membresia
    ) {
        List<MembresiaReferidoDto> referidos = service.obtenerReferidos(membresia);
        return ResponseEntity.ok(ApiResponse.success("Referidos de la membresía obtenidos exitosamente.", referidos));
    }
}
