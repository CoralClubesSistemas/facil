package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.facil.modules.cobranza.dto.request.GuardarPaqueteAnualRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.*;
import com.coralclubes.facil.modules.cobranza.service.PaqueteAnualService;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/cobranza/paquete-anual")
@RequiredArgsConstructor
public class PaqueteAnualAdminController {

    private final PaqueteAnualService service;
    private final UserContext userContext;

    @GetMapping
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<PaqueteAnualResponse>>> obtenerPaquetesAnuales(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer tipoMembresia,
            @RequestParam(required = false) Integer clasificacionMembresia,
            @RequestParam(required = false) Integer desarrollo
    ) {
        List<PaqueteAnualResponse> paquetes = service.obtenerPaquetesAnuales(anio, tipoMembresia, clasificacionMembresia, desarrollo);
        return ResponseEntity.ok(ApiResponse.success("Paquetes anuales obtenidos exitosamente.", paquetes));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<PaqueteAnualDetalleResponse>> obtenerPaqueteAnualDetalle(
            @PathVariable Integer id
    ) {
        PaqueteAnualDetalleResponse detalle = service.obtenerPaqueteAnualDetalle(id);
        return ResponseEntity.ok(ApiResponse.success("Detalle de paquete anual obtenido exitosamente.", detalle));
    }

    @GetMapping("/movimientos")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<MovimientoPaqueteAnualResponse>>> obtenerMovimientosPaqueteAnual(
            @RequestParam Integer anio,
            @RequestParam Integer tipoMembresia
    ) {
        List<MovimientoPaqueteAnualResponse> movimientos = service.obtenerMovimientosPaqueteAnual(anio, tipoMembresia);
        return ResponseEntity.ok(ApiResponse.success("Movimientos de paquete anual obtenidos exitosamente.", movimientos));
    }

    @GetMapping("/propuesta/esquemas")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<EsquemaPagoPropuestaResponse>>> obtenerEsquemasPagoPropuesta(
            @RequestParam String membresia,
            @RequestParam Integer anio
    ) {
        List<EsquemaPagoPropuestaResponse> esquemas = service.obtenerEsquemasPagoPropuesta(membresia, anio);
        return ResponseEntity.ok(ApiResponse.success("Esquemas de pago obtenidos exitosamente.", esquemas));
    }

    @PostMapping("/propuesta/cotizar")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<CotizacionPaqueteAnualResponse>> cotizarPropuestaPaqueteAnual(
            @RequestParam String membresia,
            @RequestParam Integer anio,
            @RequestBody(required = false) List<String> esquemas
    ) {
        CotizacionPaqueteAnualResponse cotizacion = service.cotizarPropuestaPaqueteAnual(membresia, anio, esquemas);
        return ResponseEntity.ok(ApiResponse.success("Cotización de paquete anual generada exitosamente.", cotizacion));
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<Integer>> guardarPaqueteAnual(
            @Valid @RequestBody GuardarPaqueteAnualRequest request
    ) {
        String usuario = userContext.getUsername();
        Integer idPaquete = service.guardarPaqueteAnual(request, usuario);
        return ResponseEntity.ok(ApiResponse.success("Paquete anual guardado exitosamente.", idPaquete));
    }
}
