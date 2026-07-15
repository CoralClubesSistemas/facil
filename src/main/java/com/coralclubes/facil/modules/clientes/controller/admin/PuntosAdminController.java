package com.coralclubes.facil.modules.clientes.controller.admin;

import com.coralclubes.facil.modules.clientes.dto.projection.DetalleCuentaPuntosProjection;
import com.coralclubes.facil.modules.clientes.dto.request.DetalleCuentaPuntosRequest;
import com.coralclubes.facil.modules.clientes.dto.response.ConsumoPuntosDto;
import com.coralclubes.facil.modules.clientes.dto.response.CuentaPuntosDto;
import com.coralclubes.facil.modules.clientes.dto.response.PuntosLiberadosDto;
import com.coralclubes.facil.modules.clientes.dto.response.DocumentoPdfDto;
import com.coralclubes.facil.modules.clientes.dto.response.PaquetesPuntosPlanResponse;
import com.coralclubes.facil.modules.clientes.service.PuntosService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/clientes/puntos")
@RequiredArgsConstructor
public class PuntosAdminController {

    private final PuntosService puntosService;

    @GetMapping("/{membresia}/consumo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ConsumoPuntosDto>>> obtenerConsumoDePuntos(
            @PathVariable String membresia,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaCorte
    ) {
        List<ConsumoPuntosDto> list = puntosService.obtenerConsumoDePuntos(membresia, fechaCorte);
        return ResponseEntity.ok(ApiResponse.success("Consumo de puntos obtenido exitosamente.", list));
    }

    @GetMapping("/{membresia}/liberados")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PuntosLiberadosDto>>> obtenerPuntosLiberados(
            @PathVariable String membresia,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaCorte
    ) {
        List<PuntosLiberadosDto> list = puntosService.obtenerPuntosLiberados(membresia, fechaCorte);
        return ResponseEntity.ok(ApiResponse.success("Puntos liberados obtenidos exitosamente.", list));
    }

    @GetMapping("/{membresia}/cuenta")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CuentaPuntosDto>>> obtenerCuentaDePuntos(
            @PathVariable String membresia,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaCorte
    ) {
        List<CuentaPuntosDto> list = puntosService.obtenerCuentaDePuntos(membresia, fechaCorte);
        return ResponseEntity.ok(ApiResponse.success("Cuenta de puntos obtenida exitosamente.", list));
    }

    @GetMapping("/{membresia}/pdfs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DocumentoPdfDto>>> obtenerPdfsPuntos(
            @PathVariable String membresia,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaCorte
    ) {
        List<DocumentoPdfDto> pdfs = puntosService.generarPdfsPuntos(membresia, fechaCorte);
        return ResponseEntity.ok(ApiResponse.success("Listado de PDFs de puntos generado exitosamente.", pdfs));
    }

    @GetMapping("/{membresia}/pdf/consumo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DocumentoPdfDto>> obtenerPdfConsumoPuntos(
            @PathVariable String membresia,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaCorte
    ) {
        DocumentoPdfDto pdf = puntosService.generarPdfConsumoPuntos(membresia, fechaCorte);
        return ResponseEntity.ok(ApiResponse.success("PDF de consumo de puntos generado exitosamente.", pdf));
    }

    @GetMapping("/{membresia}/pdf/liberados")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DocumentoPdfDto>> obtenerPdfPuntosLiberados(
            @PathVariable String membresia,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaCorte
    ) {
        DocumentoPdfDto pdf = puntosService.generarPdfPuntosLiberados(membresia, fechaCorte);
        return ResponseEntity.ok(ApiResponse.success("PDF de puntos liberados generado exitosamente.", pdf));
    }

    @GetMapping("/{membresia}/pdf/estado-cuenta")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DocumentoPdfDto>> obtenerPdfEstadoCuentaPuntos(
            @PathVariable String membresia,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaCorte
    ) {
        DocumentoPdfDto pdf = puntosService.generarPdfEstadoCuentaPuntos(membresia, fechaCorte);
        return ResponseEntity.ok(ApiResponse.success("PDF de estado de cuenta de puntos generado exitosamente.", pdf));
    }

    @GetMapping("/{membresia}/paquetes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaquetesPuntosPlanResponse>> obtenerPaquetesPuntosPlan(
            @PathVariable String membresia
    ) {
        PaquetesPuntosPlanResponse response = puntosService.obtenerPaquetesPuntosPlan(membresia);
        return ResponseEntity.ok(ApiResponse.success("Paquetes de puntos por plan obtenidos exitosamente.", response));
    }

    @GetMapping("/{membresia}/detalle-cuenta")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DetalleCuentaPuntosProjection>>> obtenerDetalleCuentaDePuntos(
            @PathVariable String membresia,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate fechaFin,
            @RequestParam(required = false) String estatusPuntos,
            @RequestParam(required = false) Integer numeroPlan
    ) {
        DetalleCuentaPuntosRequest request = DetalleCuentaPuntosRequest.builder()
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .estatusPuntos(estatusPuntos)
                .numeroPlan(numeroPlan)
                .build();
        List<DetalleCuentaPuntosProjection> list = puntosService.obtenerDetalleCuentaDePuntos(membresia, request);
        return ResponseEntity.ok(ApiResponse.success("Detalle de cuenta de puntos obtenido exitosamente.", list));
    }
}
