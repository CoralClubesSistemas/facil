package com.coralclubes.facil.modules.clientes.controller.admin;

import com.coralclubes.facil.modules.clientes.dto.response.ConsumoPuntosDto;
import com.coralclubes.facil.modules.clientes.dto.response.CuentaPuntosDto;
import com.coralclubes.facil.modules.clientes.dto.response.PuntosLiberadosDto;
import com.coralclubes.facil.modules.clientes.dto.response.DocumentoPdfDto;
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
}
