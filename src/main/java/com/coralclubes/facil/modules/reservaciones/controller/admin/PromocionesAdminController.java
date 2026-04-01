package com.coralclubes.facil.modules.reservaciones.controller.admin;

import com.coralclubes.facil.modules.reservaciones.dto.request.ConsumoOfertaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.EnlazarImagenRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.PromocionIntegralRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.Promocion;
import com.coralclubes.facil.modules.reservaciones.dto.response.PromocionListResponse;
import com.coralclubes.facil.modules.reservaciones.service.PromocionesService;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitarUrlImagenRequest;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión interna de Promociones.
 * Exclusivo para usuarios administrativos/empleados a través de Facil-Frontend.
 */
@RestController
@RequestMapping("/api/v1/admin/reservaciones/promociones")
@RequiredArgsConstructor
public class PromocionesAdminController {

    private final PromocionesService service;

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('MOD_SMNUPROMOCIONESRESERVACIONES')")
    public ResponseEntity<ApiResponse<List<PromocionListResponse>>> obtenerPromociones() {
        ApiResponse<List<PromocionListResponse>> response = service.obtenerPromociones();
        return ResponseEntity.status(response.status()).body(response);
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('MOD_SMNUPROMOCIONESRESERVACIONES')")
    public ResponseEntity<ApiResponse<Integer>> guardarPromocion(
            @Valid @RequestBody PromocionIntegralRequest request) {
        ApiResponse<Integer> response = service.guardarPromocion(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('MOD_SMNUPROMOCIONESRESERVACIONES')")
    public ResponseEntity<ApiResponse<Boolean>> eliminarPromocion(@PathVariable Integer id) {
        ApiResponse<Boolean> response = service.eliminarPromocion(id);
        return ResponseEntity.status(response.status()).body(response);
    }

    @PostMapping("/imagenes/upload-url")
    @PreAuthorize("hasAuthority('MOD_SMNUPROMOCIONESRESERVACIONES')")
    public ResponseEntity<ApiResponse<RespuestaCargaDto>> solicitarUrlCarga(
            @Valid @RequestBody SolicitarUrlImagenRequest request) {
        ApiResponse<RespuestaCargaDto> response = service.solicitarUrlCarga(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    @PutMapping("/imagenes/enlazar")
    @PreAuthorize("hasAuthority('MOD_SMNUPROMOCIONESRESERVACIONES')")
    public ResponseEntity<ApiResponse<Boolean>> enlazarImagen(
            @Valid @RequestBody EnlazarImagenRequest request) {
        return ResponseEntity.ok(service.enlazarImagenPromocion(request));
    }

    /**
     * Endpoint abierto. Permite que un cliente valide un código en su carrito
     * para ver si existe y qué beneficios da, incluso antes de hacer login.
     */
    @GetMapping("/validar/{codigo}")
    @PreAuthorize("hasAuthority('MOD_SMNUPROMOCIONESRESERVACIONES')")
    public ResponseEntity<ApiResponse<Promocion>> validarCodigo(@PathVariable String codigo) {
        ApiResponse<Promocion> response = service.validarPromocionPorCodigo(codigo);
        return ResponseEntity.status(response.status()).body(response);
    }

    @PostMapping("/aplicar-consumo")
    @PreAuthorize("hasAuthority('MOD_SMNUPROMOCIONESRESERVACIONES')")
    public ResponseEntity<ApiResponse<Integer>> aplicarConsumo(
            @Valid @RequestBody ConsumoOfertaRequest request) {
        ApiResponse<Integer> response = service.aplicarConsumoOferta(request);
        return ResponseEntity.status(response.status()).body(response);
    }
}