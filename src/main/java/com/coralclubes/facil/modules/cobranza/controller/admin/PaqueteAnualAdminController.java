package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.facil.modules.cobranza.dto.request.CotizarPropuestaPaqueteAnualRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GuardarPaqueteAnualRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GuardarPropuestaPaqueteAnualRequest;
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

    @GetMapping("/propuesta/cupones")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<CuponBeneficioPaqueteAnualResponse>>> obtenerCuponesBeneficio(
            @RequestParam String membresia,
            @RequestParam Integer anio,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime fechaCotizacion
    ) {
        List<CuponBeneficioPaqueteAnualResponse> cupones = service.obtenerCuponesBeneficio(membresia, anio, fechaCotizacion);
        return ResponseEntity.ok(ApiResponse.success("Cupones de beneficio obtenidos exitosamente.", cupones));
    }

    @PostMapping("/propuesta/cotizar")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<CotizacionPaqueteAnualResponse>> cotizarPropuestaPaqueteAnual(
            @Valid @RequestBody CotizarPropuestaPaqueteAnualRequest request
    ) {
        CotizacionPaqueteAnualResponse cotizacion = service.cotizarPropuestaPaqueteAnual(request);
        return ResponseEntity.ok(ApiResponse.success("Cotización de paquete anual generada exitosamente.", cotizacion));
    }

    @GetMapping("/propuesta")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<PropuestaPaqueteAnualResponse>> obtenerPropuestaPaqueteAnual(
            @RequestParam String membresia,
            @RequestParam Integer anio
    ) {
        PropuestaPaqueteAnualResponse propuesta = service.obtenerPropuestaPaqueteAnual(membresia, anio);
        return ResponseEntity.ok(ApiResponse.success("Propuesta de paquete anual consultada exitosamente.", propuesta));
    }

    @GetMapping("/propuesta/correo")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<CuerpoCorreoResponse>> sintetizarCuerpoCorreoPropuesta(
            @RequestParam String membresia,
            @RequestParam Integer anio
    ) {
        CuerpoCorreoResponse cuerpoCorreo = service.sintetizarCuerpoCorreoPropuesta(membresia, anio);
        return ResponseEntity.ok(ApiResponse.success("Cuerpo de correo sintetizado exitosamente.", cuerpoCorreo));
    }

    @PostMapping("/propuesta/guardar")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<Integer>> guardarPropuestaPaqueteAnual(
            @Valid @RequestBody GuardarPropuestaPaqueteAnualRequest request
    ) {
        String usuario = userContext.getUsername();
        Integer propuestaId = service.guardarPropuestaPaqueteAnual(request, usuario);
        return ResponseEntity.ok(ApiResponse.success("Propuesta de paquete anual guardada exitosamente.", propuestaId));
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
