package com.coralclubes.facil.modules.reservaciones.controller.admin;

import com.coralclubes.facil.modules.reservaciones.dto.request.AgregarOtaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.ConfiguracionUnidadesRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.CrearConfiguracionOtaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.GenerarReservacionOtaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.ConfiguracionOtaResponse;
import com.coralclubes.facil.modules.reservaciones.dto.response.GenerarReservacionOtaResponse;
import com.coralclubes.facil.modules.reservaciones.dto.response.UnidadOtaResponse;
import com.coralclubes.facil.modules.reservaciones.service.OtasService;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reservaciones/otas")
@RequiredArgsConstructor
public class OtasController {

    private final OtasService otasService;
    private final UserContext userContext;

    @PostMapping
    @PreAuthorize("hasAuthority('MOD_SMNURESERVACIONES')")
    public ResponseEntity<ApiResponse<Void>> agregarOta(@Valid @RequestBody AgregarOtaRequest request) {
        otasService.agregarOta(request);
        return ResponseEntity.ok(ApiResponse.success("OTA agregada exitosamente.", null));
    }

    @PostMapping("/configuraciones/save")
    @PreAuthorize("hasAuthority('MOD_SMNURESERVACIONES')")
    public ResponseEntity<ApiResponse<Integer>> crearConfiguracionOta(@Valid @RequestBody CrearConfiguracionOtaRequest request) {
        String usuario = userContext.getUsername();
        Integer idConfiguracionOta = otasService.crearConfiguracionOta(request, usuario);
        return ResponseEntity.ok(ApiResponse.success("Configuración de OTA creada exitosamente.", idConfiguracionOta));
    }

    @PostMapping("/configuraciones/{idConfiguracionOta}/unidades")
    @PreAuthorize("hasAuthority('MOD_SMNURESERVACIONES')")
    public ResponseEntity<ApiResponse<Integer>> agregarUnidadesConfiguracionOta(
            @PathVariable Integer idConfiguracionOta,
            @Valid @RequestBody ConfiguracionUnidadesRequest request) {
        String usuario = userContext.getUsername();
        Integer resultId = otasService.agregarUnidadesConfiguracionOta(idConfiguracionOta, request, usuario);
        return ResponseEntity.ok(ApiResponse.success("Unidades agregadas a la configuración de OTA exitosamente.", resultId));
    }

    @DeleteMapping("/configuraciones/{idConfiguracionOta}/unidades")
    @PreAuthorize("hasAuthority('MOD_SMNURESERVACIONES')")
    public ResponseEntity<ApiResponse<Integer>> eliminarUnidadesConfiguracionOta(
            @PathVariable Integer idConfiguracionOta,
            @Valid @RequestBody ConfiguracionUnidadesRequest request) {
        String usuario = userContext.getUsername();
        Integer resultId = otasService.eliminarUnidadesConfiguracionOta(idConfiguracionOta, request, usuario);
        return ResponseEntity.ok(ApiResponse.success("Unidades eliminadas de la configuración de OTA exitosamente.", resultId));
    }

    @PutMapping("/configuraciones/{idConfiguracionOta}/desactivar")
    @PreAuthorize("hasAuthority('MOD_SMNURESERVACIONES')")
    public ResponseEntity<ApiResponse<Integer>> desactivarConfiguracionOta(
            @PathVariable Integer idConfiguracionOta) {
        String usuario = userContext.getUsername();
        Integer resultId = otasService.desactivarConfiguracionOta(idConfiguracionOta, usuario);
        return ResponseEntity.ok(ApiResponse.success("Configuración de OTA desactivada exitosamente.", resultId));
    }

    @GetMapping("/configuraciones")
    @PreAuthorize("hasAuthority('MOD_SMNURESERVACIONES')")
    public ResponseEntity<ApiResponse<List<ConfiguracionOtaResponse>>> obtenerConfiguracionesOtas() {
        List<ConfiguracionOtaResponse> configuraciones = otasService.obtenerConfiguracionesOtas();
        return ResponseEntity.ok(ApiResponse.success("Configuraciones de OTAs obtenidas exitosamente.", configuraciones));
    }

    @GetMapping("/configuraciones/{idConfiguracionOta}/unidades")
    @PreAuthorize("hasAuthority('MOD_SMNURESERVACIONES')")
    public ResponseEntity<ApiResponse<List<UnidadOtaResponse>>> obtenerUnidadesConfiguracionOta(
            @PathVariable Integer idConfiguracionOta) {
        List<UnidadOtaResponse> unidades = otasService.obtenerUnidadesConfiguracionOta(idConfiguracionOta);
        return ResponseEntity.ok(ApiResponse.success("Unidades asociadas a la configuración obtenidas exitosamente.", unidades));
    }

    @GetMapping("/configuraciones/{idConfiguracionOta}/unidades/disponibles")
    @PreAuthorize("hasAuthority('MOD_SMNURESERVACIONES')")
    public ResponseEntity<ApiResponse<List<UnidadOtaResponse>>> obtenerUnidadesDisponiblesParaOta(
            @PathVariable Integer idConfiguracionOta,
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin) {
        List<UnidadOtaResponse> unidades = otasService.obtenerUnidadesDisponiblesParaOta(idConfiguracionOta, fechaInicio, fechaFin);
        return ResponseEntity.ok(ApiResponse.success("Unidades disponibles para asociar a la OTA obtenidas exitosamente.", unidades));
    }

    @GetMapping("/disponibilidad")
    @PreAuthorize("hasAuthority('MOD_SMNURESERVACIONES')")
    public ResponseEntity<ApiResponse<List<UnidadOtaResponse>>> buscarDisponibilidadUnidadesOta(
            @RequestParam Integer idDesarrollo,
            @RequestParam Integer tipoUnidad,
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin) {
        List<UnidadOtaResponse> unidades = otasService.buscarDisponibilidadUnidadesOta(idDesarrollo, tipoUnidad, fechaInicio, fechaFin);
        return ResponseEntity.ok(ApiResponse.success("Búsqueda de disponibilidad completada exitosamente.", unidades));
    }

    @PostMapping("/reservaciones")
    @PreAuthorize("hasAuthority('MOD_SMNURESERVACIONES')")
    public ResponseEntity<ApiResponse<GenerarReservacionOtaResponse>> generarReservacionOta(
            @Valid @RequestBody GenerarReservacionOtaRequest request) {
        String usuario = userContext.getUsername();
        GenerarReservacionOtaResponse response = otasService.generarReservacionOta(request, usuario);
        return ResponseEntity.ok(ApiResponse.success("Reservación desde OTA generada exitosamente.", response));
    }
}
