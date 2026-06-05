package com.coralclubes.facil.modules.reservaciones.controller.admin;

import com.coralclubes.facil.modules.reservaciones.dto.request.*;
import com.coralclubes.facil.modules.reservaciones.dto.response.*;
import com.coralclubes.facil.modules.reservaciones.service.ReservacionesService;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/recepcion")
@RequiredArgsConstructor
public class RecepcionController {

    private final ReservacionesService recepcionService;
    private final UserContext userContext;

    @GetMapping("/operaciones/hoy")
    public ResponseEntity<ApiResponse<List<OperacionDiaDto>>> obtenerOperacionesHoy() {
        return ResponseEntity.ok(recepcionService.obtenerOperacionesDelDia());
    }

    @GetMapping("/estadisticas/hoy")
    public ResponseEntity<ApiResponse<List<EstadisticaDelDiaDto>>> obtenerEstadisticasHoy() {
        return ResponseEntity.ok(recepcionService.obtenerEstadisticasDelDia());
    }

    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<Boolean>> registrarCheckIn(@Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.ok(recepcionService.registrarCheckIn(request));
    }

    @GetMapping("/operaciones/detalle")
    public ResponseEntity<ApiResponse<DetalleReservacionDto>> obtenerDetalleReservacion(
            @RequestParam String membresia,
            @RequestParam Integer consecutivo) {

        var response = recepcionService.obtenerDetalleReservacion(membresia, consecutivo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unidades-check-in")
    public ResponseEntity<ApiResponse<List<UnidadDisponibleDto>>> obtenerUnidadesDisponiblesCheckIn(
            @RequestParam String membresia,
            @RequestParam Integer consecutivo,
            @RequestParam Integer rhdtId
    ) {

        var response = recepcionService.obtenerUnidadesDisponiblesCheckIn(membresia, consecutivo, rhdtId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-out")
    public ResponseEntity<ApiResponse<Boolean>> registrarCheckOut(@Valid @RequestBody CheckOutRequest request) {
        return ResponseEntity.ok(recepcionService.registrarCheckOut(request));
    }

    @GetMapping("/check-in-out-especial/cotizar")
    public ResponseEntity<ApiResponse<CheckInOutEspecialCotizacionDto>> cotizarCheckInOutEspecial(
            @RequestParam String membresia,
            @RequestParam Integer consecutivo) {
        System.out.println("Recibido cotizarCheckInOutEspecial para membresia: " + membresia + ", consecutivo: " + consecutivo);

        return ResponseEntity.ok(recepcionService.cotizarCheckInOutEspecial(membresia, consecutivo));
    }

    @PostMapping("/check-in-out-especial")
    public ResponseEntity<ApiResponse<Boolean>> registrarMovimientoCheckInOutEspecial(
            @Valid @RequestBody CheckInOutEspecialRequest request) {
        return ResponseEntity.ok(recepcionService.registrarMovimientoCheckInOutEspecial(request));
    }

    @GetMapping("/operaciones/cargos")
    public ResponseEntity<ApiResponse<List<CargoHabitacionDto>>> obtenerCargosReservacion(
            @RequestParam String membresia,
            @RequestParam Integer consecutivo) {

        var response = recepcionService.obtenerCargosReservacion(membresia, consecutivo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cargos/catalogo")
    public ResponseEntity<ApiResponse<List<CatalogoCargoDto>>> obtenerCatalogoCargos(
            @RequestParam String membresia) {
        return ResponseEntity.ok(recepcionService.obtenerCatalogoCargos(membresia));
    }

    @PostMapping("/cargos")
    public ResponseEntity<ApiResponse<Boolean>> generarCargoHabitacion(
            @Valid @RequestBody GenerarCargoRequest request) {
        return ResponseEntity.ok(recepcionService.generarCargo(request));
    }

    @GetMapping("/unidades/mapa")
    public ResponseEntity<ApiResponse<List<MapaUnidadDto>>> obtenerMapaUnidades() {
        return ResponseEntity.ok(recepcionService.obtenerMapaUnidades());
    }

    @GetMapping("/actividad-reciente")
    public ResponseEntity<ApiResponse<List<String>>> obtenerActividadReciente() {
        return ResponseEntity.ok(recepcionService.obtenerActividadReciente());
    }

    @PostMapping("/transferir-unidad")
    public ResponseEntity<ApiResponse<Boolean>> transferirUnidad(
            @Valid @RequestBody TransferirUnidadRequest request) {

        var response = recepcionService.transferirUnidad(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping ("/diferencia-transferencia")
    public ResponseEntity<ApiResponse<BigDecimal>> calcularDiferenciaTransferencia(
            @RequestParam String membresia,
            @RequestParam Integer consecutivo,
            @RequestParam Integer nuevoTipoUnidadId) {
        var response = recepcionService.calcularDiferenciaTransferencia(membresia, consecutivo, nuevoTipoUnidadId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cancelacion/cotizar-penalizacion")
    public ResponseEntity<ApiResponse<BigDecimal>> cotizarPenalizacionCancelacion(
            @RequestParam String membresia,
            @RequestParam Integer consecutivo) {

        var response = recepcionService.calcularPenalizacionCancelacion(membresia, consecutivo);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancelacion/ejecutar")
    public ResponseEntity<ApiResponse<Boolean>> cancelarReservacion(
            @Valid @RequestBody CancelarReservacionRequest request) {
        String usuario = userContext.getUsername();

        var response = recepcionService.cancelarReservacion(request, usuario);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generar-orden-adeudo")
    @PreAuthorize("hasAuthority('MOD_SMNURESERVACIONES')")
    public ResponseEntity<ApiResponse<UUID>> generarOrdenCobranzaSaldosPendientes(
            @RequestBody ReservacionInfoRequest request
    ) {
        ApiResponse<UUID> response = ApiResponse.success(recepcionService.generarOrdenSaldosPendientes(request.membresia(), request.folio()));
        return ResponseEntity.ok(response);
    }
}