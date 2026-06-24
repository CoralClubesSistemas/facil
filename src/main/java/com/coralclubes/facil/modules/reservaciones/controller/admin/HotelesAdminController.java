package com.coralclubes.facil.modules.reservaciones.controller.admin;

import com.coralclubes.facil.modules.reservaciones.dto.request.*;
import com.coralclubes.facil.modules.reservaciones.dto.response.CaracteristicaDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.HotelCardUI;
import com.coralclubes.facil.modules.reservaciones.dto.response.HotelDetalleDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.HotelesCardList;
import com.coralclubes.facil.modules.reservaciones.service.HotelesService;
import com.coralclubes.facil.shared.domain.dto.ImagenResponse;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitarUrlRequest;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST Administrativo para el Submódulo de Hoteles.
 * Proporciona los endpoints de escritura que requieren autenticación y permisos.
 */
@RestController
@RequestMapping("/api/v1/admin/reservaciones/hoteles")
@RequiredArgsConstructor
public class HotelesAdminController {

    private final HotelesService service;
    private final UserContext userContext;

    // =========================================================================
    // ENDPOINTS DE LECTURA (admin, equivalente a public)
    // =========================================================================

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<HotelCardUI>>> obtenerHotelesCard() {
        Integer idDesarrollo = userContext.getIdDesarrollo();
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

    @GetMapping("/desactivados")
    public ResponseEntity<ApiResponse<List<HotelesCardList>>> obtenerHotelesDesactivados() {
        return ResponseEntity.ok(ApiResponse.success(service.obtenerHotelesDesactivados()));
    }

    // =========================================================================
    // ENDPOINTS DE ESCRITURA (POST / PUT / DELETE)
    // =========================================================================

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('MOD_SMNUHOTELES')")
    public ResponseEntity<ApiResponse<Integer>> guardarHotel(
            @Valid @RequestBody HotelRequest hotel) {
        ApiResponse<Integer> response = service.guardarHotel(hotel);
        return ResponseEntity.status(response.status()).body(response);
    }

    @PostMapping("/caracteristicas")
    @PreAuthorize("hasAuthority('MOD_SMNUHOTELES')")
    public ResponseEntity<ApiResponse<Integer>> guardarCaracteristicasHotel(
            @Valid @RequestBody GuardarCaracteristicasRequest request) {
        ApiResponse<Integer> response = service.guardarCaracteristicasHotel(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    @PostMapping("/imagenes")
    @PreAuthorize("hasAuthority('MOD_SMNUHOTELES')")
    public ResponseEntity<ApiResponse<Integer>> guardarImagenesHotel(
            @Valid @RequestBody GuardarImagenesRequest request) {
        ApiResponse<Integer> response = service.guardarImagenesHotel(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    @PutMapping("/imagenes/portada")
    @PreAuthorize("hasAuthority('MOD_SMNUHOTELES')")
    public ResponseEntity<ApiResponse<Boolean>> cambiarImagenPortadaHotel(
            @Valid @RequestBody CambiarPortadaRequest request) {
        ApiResponse<Boolean> response = service.cambiarImagenPortadaHotel(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    @DeleteMapping("/imagenes")
    @PreAuthorize("hasAuthority('MOD_SMNUHOTELES')")
    public ResponseEntity<ApiResponse<Boolean>> eliminarImagenesHotel(
            @Valid @RequestBody EliminarImagenesRequest request) {
        ApiResponse<Boolean> response = service.eliminarImagenesHotel(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('AUTH_DHTL')")
    public ResponseEntity<ApiResponse<Boolean>> desactivarHotel(
            @RequestParam Integer idHotel) {
        ApiResponse<Boolean> response = service.desactivarHotel(idHotel);
        return ResponseEntity.status(response.status()).body(response);
    }

    @PostMapping("/imagenes/upload-url")
    @PreAuthorize("hasAuthority('MOD_SMNUHOTELES')")
    public ResponseEntity<ApiResponse<RespuestaCargaDto>> solicitarUrlCarga(
            @Valid @RequestBody SolicitarUrlRequest request) {

        return ResponseEntity.ok(service.obtenerUrlCargaImagen(request));
    }

    @PostMapping("reactivar")
    @PreAuthorize("hasAuthority('AUTH_DHTL')")
    public ResponseEntity<ApiResponse<Void>> reactivarHotel(
            @RequestParam Integer idHotel) {
        service.reactivarHotel(idHotel);
        return ResponseEntity.ok(ApiResponse.empty());
    }
}