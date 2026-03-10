package com.coralclubes.facil.modules.reservaciones.controller.publico;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.reservaciones.dto.response.CaracteristicaDto;
import com.coralclubes.facil.modules.reservaciones.service.CatalogosService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST Público para proveer los catálogos comerciales.
 * Utilizado por los portales web y apps móviles (sin token).
 */
@RestController
@RequestMapping("/api/v1/public/reservaciones/catalogos")
@RequiredArgsConstructor
public class CatalogosPublicController {

    private final CatalogosService service;

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
}