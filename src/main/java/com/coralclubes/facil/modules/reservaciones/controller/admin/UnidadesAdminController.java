package com.coralclubes.facil.modules.reservaciones.controller.admin;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.reservaciones.dto.request.*;
import com.coralclubes.facil.modules.reservaciones.dto.response.*;
import com.coralclubes.facil.modules.reservaciones.service.UnidadesService;
import com.coralclubes.facil.shared.domain.dto.ImagenResponse;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitarUrlRequest;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST Administrativo para la gestión de Unidades (Tipos Lógicos y Habitaciones Físicas).
 * Proporciona los endpoints para consultar, crear, modificar y desactivar.
 */
@RestController
@RequestMapping("/api/v1/admin/reservaciones/unidades")
@RequiredArgsConstructor
public class UnidadesAdminController {

    private final UnidadesService service;
    private final UserContext userContext;

    // =========================================================================
    // TIPOS LÓGICOS DE UNIDAD (PLANTILLAS) - ESCRITURA
    // =========================================================================

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('MOD_SMNUUNIDADESRESERVACIONES')")
    public ResponseEntity<ApiResponse<Integer>> guardarTipoUnidad(
            @Valid @RequestBody TipoUnidadRequest request) {
        ApiResponse<Integer> response = service.guardarTipoUnidad(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    @PostMapping("/caracteristicas")
    @PreAuthorize("hasAuthority('MOD_SMNUUNIDADESRESERVACIONES')")
    public ResponseEntity<ApiResponse<Integer>> guardarCaracteristicasTipoUnidad(
            @Valid @RequestBody GuardarCaracteristicasRequest request) {
        ApiResponse<Integer> response = service.guardarCaracteristicasTipoUnidad(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    @PostMapping("/imagenes")
    @PreAuthorize("hasAuthority('MOD_SMNUUNIDADESRESERVACIONES')")
    public ResponseEntity<ApiResponse<Integer>> guardarImagenesTipoUnidad(
            @Valid @RequestBody GuardarImagenesRequest request) {
        ApiResponse<Integer> response = service.guardarImagenesTipoUnidad(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    @PutMapping("/imagenes/portada")
    @PreAuthorize("hasAuthority('MOD_SMNUUNIDADESRESERVACIONES')")
    public ResponseEntity<ApiResponse<Boolean>> cambiarImagenPortadaTipoUnidad(
            @Valid @RequestBody CambiarPortadaRequest request) {
        ApiResponse<Boolean> response = service.cambiarImagenPortadaTipoUnidad(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    @DeleteMapping("/imagenes")
    @PreAuthorize("hasAuthority('MOD_SMNUUNIDADESRESERVACIONES')")
    public ResponseEntity<ApiResponse<Boolean>> eliminarImagenesTipoUnidad(
            @Valid @RequestBody EliminarImagenesRequest request) {
        ApiResponse<Boolean> response = service.eliminarImagenesTipoUnidad(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('AUTH_DRUN')")
    public ResponseEntity<ApiResponse<Boolean>> desactivarTipoUnidad(
            @RequestParam Integer idTipoUnidad) {
        ApiResponse<Boolean> response = service.desactivarTipoUnidad(idTipoUnidad);
        return ResponseEntity.status(response.status()).body(response);
    }

    @PostMapping("/imagenes/upload-url")
    @PreAuthorize("hasAuthority('MOD_SMNUUNIDADESRESERVACIONES')")
    public ResponseEntity<ApiResponse<RespuestaCargaDto>> solicitarUrlCarga(
            @Valid @RequestBody SolicitarUrlRequest request) {
        ApiResponse<RespuestaCargaDto> response = service.obtenerUrlCargaImagen(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    // =========================================================================
    // TIPOS LÓGICOS DE UNIDAD - LECTURA (admin, equivalente a public)
    // =========================================================================

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<TipoUnidadUI>>> obtenerTiposUnidadCard() {
        Integer idDesarrollo = userContext.getIdDesarrollo();

        return ResponseEntity.ok(service.obtenerTiposUnidadCard(idDesarrollo));
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

    @GetMapping("/ui-detalles")
    public ResponseEntity<ApiResponse<TipoUnidadUIDetalles>> obtenerTipoUnidadUIDetalles(
            @RequestParam Integer idTipoUnidad) {
        ApiResponse<TipoUnidadUIDetalles> response = service.obtenerTipoUnidadUIDetalles(idTipoUnidad);
        return ResponseEntity.status(response.status()).body(response);
    }

    // =========================================================================
    // UNIDADES FÍSICAS - LECTURA Y ESCRITURA (100% ADMINISTRATIVO)
    // =========================================================================

    @GetMapping("/fisicas/asignadas")
    public ResponseEntity<ApiResponse<List<UnidadFisicaDto>>> obtenerUnidadesFisicasAsignadas(
            @RequestParam Integer idTipoUnidad) {
        return ResponseEntity.ok(service.obtenerUnidadesFisicasAsignadas(idTipoUnidad));
    }

    @GetMapping("/fisicas/detalle")
    public ResponseEntity<ApiResponse<DetallesUnidadFisica>> obtenerDetallesUnidadFisica(
            @RequestParam Integer idUnidadFisica
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.obtenerDetallesUnidadFisica(idUnidadFisica)));
    }

    @GetMapping("/fisicas/disponibles")
    public ResponseEntity<ApiResponse<List<UnidadFisicaDto>>> obtenerUnidadesFisicasDisponibles(
            @RequestParam Integer idDesarrollo) {
        return ResponseEntity.ok(service.obtenerUnidadesFisicasDisponibles(idDesarrollo));
    }

    @GetMapping("/fisicas/padres-disponibles")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerCatalogoPadres(
            @RequestParam Integer idDesarrollo,
            @RequestParam(required = false) Integer idUnidadExcluida) {
        return ResponseEntity.ok(service.obtenerCatalogoPadres(idDesarrollo, idUnidadExcluida));
    }

    @PostMapping("/fisicas/save")
    @PreAuthorize("hasAuthority('MOD_SMNUUNIDADESRESERVACIONES')")
    public ResponseEntity<ApiResponse<Integer>> guardarUnidadFisica(
            @Valid @RequestBody UnidadFisicaRequest request) {
        ApiResponse<Integer> response = service.guardarUnidadFisica(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    @PostMapping("/fisicas/asignar")
    @PreAuthorize("hasAuthority('MOD_SMNUUNIDADESRESERVACIONES')")
    public ResponseEntity<ApiResponse<Integer>> asignarUnidadesFisicas(
            @Valid @RequestBody AsignarUnidadesFisicasRequest request) {
        ApiResponse<Integer> response = service.asignarUnidadesFisicas(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    @PostMapping("/fisicas/desasignar")
    @PreAuthorize("hasAuthority('MOD_SMNUUNIDADESRESERVACIONES')")
    public ResponseEntity<ApiResponse<Integer>> desasignarUnidadesFisicas(
            @Valid @RequestBody DesasignarUnidadesFisicasRequest request) {
        ApiResponse<Integer> response = service.desasignarUnidadesFisicas(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    @DeleteMapping("/fisicas/delete")
    @PreAuthorize("hasAuthority('AUTH_DRUN')")
    public ResponseEntity<ApiResponse<Boolean>> desactivarUnidadFisica(
            @RequestBody DesactivarUnidadRequest request) {
        ApiResponse<Boolean> response = service.desactivarUnidadFisica(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    @GetMapping("/fisicas/bloqueadas")
    @PreAuthorize("hasAuthority('MOD_SMNUUNIDADESRESERVACIONES')")
    public ResponseEntity<ApiResponse<List<UnidadBloqueadaDto>>> obtenerUnidadesBloqueadas() {
        return ResponseEntity.ok(service.obtenerUnidadesBloqueadas());
    }

    @PostMapping("/fisicas/reactivar")
    @PreAuthorize("hasAuthority('MOD_SMNUUNIDADESRESERVACIONES')")
    public ResponseEntity<ApiResponse<Boolean>> reactivarUnidadFisica(
            @Valid @RequestBody ReactivarUnidadRequest request) {
        ApiResponse<Boolean> response = service.reactivarUnidadFisica(request);
        return ResponseEntity.status(response.status()).body(response);
    }

    @GetMapping("/amenidades/catalogo")
    @PreAuthorize("hasAuthority('MOD_SMNUUNIDADESRESERVACIONES')")
    public ResponseEntity<ApiResponse<List<ArticuloAmenidadDto>>> obtenerCatalogoAmenidades() {
        return ResponseEntity.ok(service.obtenerCatalogoAmenidades());
    }

    @GetMapping("/{idTipoUnidad}/amenidades")
    @PreAuthorize("hasAuthority('MOD_SMNUUNIDADESRESERVACIONES')")
    public ResponseEntity<ApiResponse<List<ReglaAmenidadActualDto>>> obtenerReglasAmenidades(
            @PathVariable("idTipoUnidad") Integer idTipoUnidad) {
        return ResponseEntity.ok(service.obtenerReglasAmenidades(idTipoUnidad));
    }

    @PostMapping("/amenidades/save")
    @PreAuthorize("hasAuthority('MOD_SMNUUNIDADESRESERVACIONES')")
    public ResponseEntity<ApiResponse<Boolean>> guardarReglasAmenidades(
            @Valid @RequestBody GuardarAmenidadesRequest request) {
        ApiResponse<Boolean> response = service.guardarReglasAmenidades(request);
        return ResponseEntity.status(response.status()).body(response);
    }
}