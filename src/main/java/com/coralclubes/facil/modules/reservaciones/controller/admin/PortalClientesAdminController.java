package com.coralclubes.facil.modules.reservaciones.controller.admin;

import com.coralclubes.facil.modules.reservaciones.dto.request.CancelarReservacionRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.CargoHabitacionDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.DetalleReservacionDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.ReservacionMembresiaDto;
import com.coralclubes.facil.modules.reservaciones.service.ReservacionesService;
import com.coralclubes.facil.shared.domain.dto.ArchivoDescarga;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reservaciones/portal/clientes")
@RequiredArgsConstructor
public class PortalClientesAdminController {

    private final ReservacionesService service;

    @GetMapping("/reservaciones")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ReservacionMembresiaDto>>> consultarReservacionesMembresia(
            @RequestParam String membresia) {
        return ResponseEntity.ok(service.consultarReservacionesMembresia(membresia));
    }

    @GetMapping("/detalles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DetalleReservacionDto>> obtenerDetalleReservacion(
            @RequestParam String membresia,
            @RequestParam Integer consecutivo) {
        return ResponseEntity.ok(service.obtenerDetalleReservacion(membresia, consecutivo));
    }

    @GetMapping("/cargos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CargoHabitacionDto>>> obtenerCargosReservacion(
            @RequestParam String membresia,
            @RequestParam Integer consecutivo) {
        return ResponseEntity.ok(service.obtenerCargosReservacion(membresia, consecutivo));
    }

    @GetMapping("/calcular-penalizacion")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BigDecimal>> calcularPenalizacionCancelacion(
            @RequestParam String membresia,
            @RequestParam Integer consecutivo) {
        return ResponseEntity.ok(service.calcularPenalizacionCancelacion(membresia, consecutivo));
    }

    @PostMapping("/cancelar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> cancelarReservacion(
            @Valid @RequestBody CancelarReservacionRequest request) {
        String usuario = "SOCIO";
        return ResponseEntity.ok(service.cancelarReservacion(request, usuario));
    }

    @GetMapping("/carta-ocupacion")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ArchivoDescarga>> obtenerUrlCartaOcupacion(
            @RequestParam String membresia,
            @RequestParam Integer consecutivo) {
        return ResponseEntity.ok(ApiResponse.success(service.obtenerUrlCartaOcupacion(membresia, consecutivo)));
    }

    @PostMapping("/carta-ocupacion/reenviar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> reenviarCartaOcupacion(
            @RequestParam String membresia,
            @RequestParam Integer consecutivo,
            @RequestParam String correos) {
        service.reenviarCartaOcupacion(membresia, consecutivo, correos);
        return ResponseEntity.ok(ApiResponse.success("Carta de ocupación reenviada correctamente", true));
    }
}
