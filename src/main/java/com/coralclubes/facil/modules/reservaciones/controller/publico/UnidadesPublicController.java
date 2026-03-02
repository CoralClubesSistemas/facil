package com.coralclubes.facil.modules.reservaciones.controller.publico;

import com.coralclubes.facil.modules.reservaciones.dto.response.CaracteristicaDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.ImagenResponse;
import com.coralclubes.facil.modules.reservaciones.dto.response.TipoUnidadDetalleDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.TipoUnidadUI;
import com.coralclubes.facil.modules.reservaciones.service.UnidadesService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST Público para el Submódulo de Unidades (Habitaciones).
 * Proporciona los endpoints de lectura de los Tipos de Habitación para portales.
 */
@RestController
@RequestMapping("/api/v1/public/reservaciones/unidades")
@RequiredArgsConstructor
public class UnidadesPublicController {

    private final UnidadesService service;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<TipoUnidadUI>>> obtenerTiposUnidadCard() {
        return ResponseEntity.ok(service.obtenerTiposUnidadCard());
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<TipoUnidadDetalleDto>> obtenerTipoUnidadDetalles(
            @RequestParam Integer idTipoUnidad) {
        ApiResponse<TipoUnidadDetalleDto> response = service.obtenerTipoUnidadDetalles(idTipoUnidad);
        return ResponseEntity.status(response.status()).body(response);
    }

    @GetMapping("/imagenes")
    public ResponseEntity<ApiResponse<List<ImagenResponse>>> obtenerTipoUnidadImagenes(
            @RequestParam Integer idTipoUnidad) {
        return ResponseEntity.ok(service.obtenerTipoUnidadImagenes(idTipoUnidad));
    }

    @GetMapping("/caracteristicas")
    public ResponseEntity<ApiResponse<List<CaracteristicaDto>>> obtenerCaracteristicasXTipoUnidad(
            @RequestParam Integer idTipoUnidad) {
        return ResponseEntity.ok(service.obtenerCaracteristicasXTipoUnidad(idTipoUnidad));
    }
}