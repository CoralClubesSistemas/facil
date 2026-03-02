package com.coralclubes.facil.modules.reservaciones.controller.publico;

import com.coralclubes.facil.modules.reservaciones.dto.response.TemporadaDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.TemporadaFechaResponse;
import com.coralclubes.facil.modules.reservaciones.service.TemporadasService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST Público para el Submódulo de Temporadas.
 * Proporciona los endpoints de lectura para mostrar calendarios de temporadas en los portales.
 */
@RestController
@RequestMapping("/api/v1/public/reservaciones/temporadas")
@RequiredArgsConstructor
public class TemporadasPublicController {

    private final TemporadasService service;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<TemporadaDto>>> obtenerTemporadas(
            @RequestParam(required = false) Integer anio) {

        return ResponseEntity.ok(service.obtenerTemporadas(anio));
    }

    @GetMapping("/fecha")
    public ResponseEntity<ApiResponse<List<TemporadaFechaResponse>>> obtenerTemporadasPorFecha(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(service.obtenerTemporadasPorFecha(fecha));
    }
}