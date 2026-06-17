package com.coralclubes.facil.modules.reservaciones.controller.admin;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.reservaciones.dto.response.CaracteristicaDto;
import com.coralclubes.facil.modules.reservaciones.service.CatalogosService;
import com.coralclubes.facil.modules.reservaciones.dto.request.GuardarCaracteristicaRequest;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST Administrativo para proveer catálogos técnicos y operativos.
 * Utilizado exclusivamente por el sistema interno (Facil Core).
 */
@RestController
@RequestMapping("/api/v1/admin/reservaciones/catalogos")
@RequiredArgsConstructor
public class CatalogosAdminController {

    private final CatalogosService service;

    // =========================================================================
    // CATÁLOGOS COMERCIALES (admin, equivalente a public)
    // =========================================================================

    @GetMapping("/hoteles")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerHoteles() {
        return ResponseEntity.ok(service.obtenerHoteles());
    }

    @GetMapping("/tipos-habitacion")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerTiposHabitaciones() {
        return ResponseEntity.ok(service.obtenerTiposHabitaciones());
    }

    @GetMapping("/destinos")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerDestinos() {
        return ResponseEntity.ok(service.obtenerDestinos());
    }

    @GetMapping("/temporadas")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerTemporadas() {
        return ResponseEntity.ok(service.obtenerTemporadas());
    }

    @GetMapping("/caracteristicas")
    public ResponseEntity<ApiResponse<List<CaracteristicaDto>>> obtenerCaracteristicas() {
        return ResponseEntity.ok(service.obtenerCaracteristicas());
    }

    // =========================================================================
    // CATÁLOGOS TÉCNICOS Y OPERATIVOS
    // =========================================================================

    @GetMapping("/tipos-accesos")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerTiposAccesos() {
        return ResponseEntity.ok(service.obtenerTiposAccesos());
    }

    @GetMapping("/tipos-tarifas")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerTiposTarifas() {
        return ResponseEntity.ok(service.obtenerTiposTarifas());
    }

    @GetMapping("/origenes-reserva")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerOrigenesReservas() {
        return ResponseEntity.ok(service.obtenerOrigenesReservas());
    }

    @GetMapping("/periodos-tarifa")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerPeriodoTarifa() {
        return ResponseEntity.ok(service.obtenerPeriodoTarifa());
    }

    @GetMapping("/tipos-unidades")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerTiposUnidades(
            @RequestParam(required = false) String idHoteles) {
        return ResponseEntity.ok(service.obtenerTiposUnidades(idHoteles));
    }

    @GetMapping("/tipos-promociones")
    public ResponseEntity<ApiResponse<List<SelectGenerico<String>>>> obtenerTiposPromociones() {
        return ResponseEntity.ok(service.obtenerTiposPromociones());
    }

    @GetMapping("/tipos-oferta")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerTiposOferta() {
        return ResponseEntity.ok(service.obtenerTiposOferta());
    }

    @GetMapping("/acciones-objetivo")
    public ResponseEntity<ApiResponse<List<SelectGenerico<String>>>> obtenerAccionesObjetivo() {
        return ResponseEntity.ok(service.obtenerAccionesObjetivo());
    }

    @GetMapping("/tipos-reglas")
    public ResponseEntity<ApiResponse<List<SelectGenerico<String>>>> obtenerTiposReglas() {
        return ResponseEntity.ok(service.obtenerTiposReglas());
    }

    @GetMapping("/comparadores")
    public ResponseEntity<ApiResponse<List<SelectGenerico<String>>>> obtenerComparadores() {
        return ResponseEntity.ok(service.obtenerComparadores());
    }

    @GetMapping("/cargos-habitacion")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerCargosHabitacion(
            @RequestParam String membresia) {
        return ResponseEntity.ok(service.obtenerCargosHabitacion(membresia));
    }

    @GetMapping("/otas")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerCatalogoOtas() {
        return ResponseEntity.ok(service.obtenerCatalogoOtas());
    }

    @GetMapping("/tipos-caracteristicas")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerTiposCaracteristicas() {
        return ResponseEntity.ok(service.obtenerTiposCaracteristicas());
    }

    @PostMapping("/caracteristicas/save")
    public ResponseEntity<ApiResponse<Boolean>> guardarCaracteristica(
            @Valid @RequestBody GuardarCaracteristicaRequest request) {
        ApiResponse<Boolean> response = service.guardarCaracteristica(request);
        return ResponseEntity.status(response.status()).body(response);
    }
}