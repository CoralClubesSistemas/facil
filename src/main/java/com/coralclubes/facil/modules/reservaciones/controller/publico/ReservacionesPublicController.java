package com.coralclubes.facil.modules.reservaciones.controller.publico;

import com.coralclubes.facil.modules.clientes.dto.response.CuponDisponibleDto;
import com.coralclubes.facil.modules.cobranza.dto.response.ConfirmacionReservaResponse;
import com.coralclubes.facil.modules.reservaciones.dto.request.*;
import com.coralclubes.facil.modules.reservaciones.dto.response.DisponibilidadUnidadDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.DisponibilidadUnidadUI;
import com.coralclubes.facil.modules.reservaciones.dto.response.ResumenCheckoutResponse;
import com.coralclubes.facil.modules.reservaciones.service.ReservacionesService;
import com.coralclubes.facil.shared.domain.dto.ArchivoDescarga;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.coralclubes.facil.shared.utils.ClientIpUtil;
import com.coralclubes.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/reservaciones")
@RequiredArgsConstructor
public class ReservacionesPublicController {

    private final ReservacionesService service;
    private final ClientIpUtil clientIpUtil;
    private final UserContext userContext;

    @PostMapping("/buscar-disponibilidad")
    public ResponseEntity<ApiResponse<List<DisponibilidadUnidadUI>>> buscarDisponibilidad(
            @Valid @RequestBody BusquedaDisponibilidadRequest request) {
        return ResponseEntity.ok(service.buscarDisponibilidad(request));
    }

    @PostMapping("/bloquear-inventario")
    public ResponseEntity<ApiResponse<UUID>> bloquearInventario(
            @Valid @RequestBody CrearReservaTemporalRequest request,
            HttpServletRequest httpRequest) {

        // Uso limpio y reutilizable de la extracción de IP
        String ipAddress = clientIpUtil.getClientIpAddress(httpRequest);

        return ResponseEntity.ok(service.bloquearInventarioTemporal(request, ipAddress));
    }

    @DeleteMapping("/liberar-inventario/{groupId}")
    public ResponseEntity<ApiResponse<Boolean>> liberarInventario(@PathVariable UUID groupId) {
        return ResponseEntity.ok(service.liberarInventario(groupId));
    }

    @PostMapping("/calcular-checkout")
    public ResponseEntity<ApiResponse<ResumenCheckoutResponse>> calcularCheckout(
            @Valid @RequestBody CalcularCheckoutRequest request) {

        return ResponseEntity.ok(service.calcularCheckout(request));
    }

    @GetMapping("/cupones/{groupId}")
    public ResponseEntity<ApiResponse<List<CuponDisponibleDto>>> obtenerCuponesDisponibles(@PathVariable UUID groupId) {
        return ResponseEntity.ok(service.obtenerCuponesDisponibles(groupId));
    }

    @PostMapping("/confirmar-orden")
    public ResponseEntity<ApiResponse<ConfirmacionReservaResponse>> confirmarReservacionConOrden(
            @Valid @RequestBody ConfirmarReservaRequest request) {

        return ResponseEntity.ok(service.confirmarReservacionConOrden(request));
    }

    @PostMapping("/crear-membresia-externo")
    public ResponseEntity<ApiResponse<String>> crearMembresiaExterno(
            @Valid @RequestBody CrearMembresiaExternoRequest request) {
        String usuario = userContext.getUsername();

        return ResponseEntity.ok(service.crearMembresiaExterno(request, usuario));
    }

    @GetMapping("/carta-ocupacion/url")
    public ResponseEntity<ApiResponse<ArchivoDescarga>> obtenerUrlCartaOcupacion(
            @RequestParam String membresia,
            @RequestParam Integer consecutivo) {
        return ResponseEntity.ok(ApiResponse.success(service.obtenerUrlCartaOcupacion(membresia, consecutivo)));
    }

    @PostMapping("/carta-ocupacion/reenviar")
    public ResponseEntity<ApiResponse<Boolean>> reenviarCartaOcupacion(
            @RequestParam String membresia,
            @RequestParam Integer consecutivo,
            @RequestParam String correos
    ) {
        service.reenviarCartaOcupacion(membresia, consecutivo, correos);
        return ResponseEntity.ok(ApiResponse.success("Carta de ocupación reenviada correctamente", true));
    }
    
    @GetMapping("/disponibilidad-unidad")
    public ResponseEntity<ApiResponse<DisponibilidadUnidadDto>> obtenerDisponibilidadUnidad(
            @RequestParam(required = true) Integer tipoUnidad,
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin,
            @RequestParam String membresia
    ) {
        return ResponseEntity.ok(service.obtenerDisponibilidadUnidad(tipoUnidad, membresia, fechaInicio, fechaFin));
    }

    @PostMapping("/confirmar-con-pago")
    public ResponseEntity<ApiResponse<String>> confirmarReservacionPortalConPago(
            @Valid @RequestBody ConfirmarReservaRequest request) {
        return ResponseEntity.ok(service.confirmarReservacionPortalConPago(request));
    }

    @PostMapping("/confirmar-sin-pago")
    public ResponseEntity<ApiResponse<List<Integer>>> confirmarReservacion(
            @Valid @RequestBody ConfirmarReservaRequest request) {
        return ResponseEntity.ok(service.confirmarReservacion(request));
    }
}