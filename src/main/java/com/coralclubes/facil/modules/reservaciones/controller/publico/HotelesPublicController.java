package com.coralclubes.facil.modules.reservaciones.controller.publico;

import com.coralclubes.facil.modules.reservaciones.dto.response.CaracteristicaDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.HotelCardUI;
import com.coralclubes.facil.modules.reservaciones.dto.response.HotelDetalleDto;
import com.coralclubes.facil.shared.infrastructure.domain.dto.ImagenResponse;
import com.coralclubes.facil.modules.reservaciones.service.HotelesService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST Público para el Submódulo de Hoteles.
 * Proporciona los endpoints de lectura para portales sin necesidad de token.
 */
@RestController
@RequestMapping("/api/v1/public/reservaciones/hoteles")
@RequiredArgsConstructor
public class HotelesPublicController {

    private final HotelesService service;

    // =========================================================================
    // ENDPOINTS DE LECTURA (GET)
    // =========================================================================

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<HotelCardUI>>> obtenerHotelesCard(
            @RequestParam(required = false) Integer idDesarrollo) {
        return ResponseEntity.ok(service.obtenerHotelesCard(idDesarrollo));
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<HotelDetalleDto>> obtenerHotelDetalles(
            @RequestParam Integer idDesarrollo) {
        ApiResponse<HotelDetalleDto> response = service.obtenerHotelDetalles(idDesarrollo);
        return ResponseEntity.status(response.status()).body(response);
    }

    @GetMapping("/imagenes")
    public ResponseEntity<ApiResponse<List<ImagenResponse>>> obtenerHotelImagenes(
            @RequestParam Integer idDesarrollo) {
        return ResponseEntity.ok(service.obtenerHotelImagenes(idDesarrollo));
    }

    @GetMapping("/caracteristicas")
    public ResponseEntity<ApiResponse<List<CaracteristicaDto>>> obtenerCaracteristicasXHotel(
            @RequestParam Integer idDesarrollo) {
        return ResponseEntity.ok(service.obtenerCaracteristicasXHotel(idDesarrollo));
    }
}