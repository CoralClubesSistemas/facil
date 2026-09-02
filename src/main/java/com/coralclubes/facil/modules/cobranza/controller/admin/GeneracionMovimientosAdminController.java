package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.facil.modules.cobranza.dto.request.GeneracionMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.CotizacionCredencialesResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoPorTipoMembresiaResponse;
import com.coralclubes.facil.modules.cobranza.service.GeneracionMovimientosService;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/cobranza/generacion-movimientos")
@RequiredArgsConstructor
public class GeneracionMovimientosAdminController {

    private final GeneracionMovimientosService service;
    private final UserContext userContext;

    @GetMapping("/tipos-movimientos")
    @PreAuthorize("hasAuthority('MOD_SMNUGENERAMOVIMIENTOS')")
    public ResponseEntity<ApiResponse<List<MovimientoPorTipoMembresiaResponse>>> obtenerTiposMovimientos(
            @RequestParam String membresia
    ) {
        List<MovimientoPorTipoMembresiaResponse> movimientos = service.obtenerMovimientosPorTipoMembresia(membresia);
        return ResponseEntity.ok(ApiResponse.success(
                "Tipos de movimientos obtenidos exitosamente.",
                movimientos
        ));
    }

    @GetMapping("/cotizar-credenciales")
    @PreAuthorize("hasAuthority('MOD_SMNUGENERAMOVIMIENTOS')")
    public ResponseEntity<ApiResponse<CotizacionCredencialesResponse>> cotizarCredenciales(
            @RequestParam String membresia,
            @RequestParam(defaultValue = "1") Integer anios,
            @RequestParam(defaultValue = "false") Boolean incluirPrevios,
            @RequestParam(required = false) Integer desarrolloConsumo
    ) {
        CotizacionCredencialesResponse cotizacion = service.cotizarCredenciales(
                membresia,
                anios,
                incluirPrevios,
                desarrolloConsumo
        );
        return ResponseEntity.ok(ApiResponse.success(
                "Cotización de credenciales consultada exitosamente.",
                cotizacion
        ));
    }

    @PostMapping("/generar")
    @PreAuthorize("hasAuthority('MOD_SMNUGENERAMOVIMIENTOS')")
    public ResponseEntity<ApiResponse<Void>> generarMovimiento(
            @Valid @RequestBody GeneracionMovimientoRequest request
    ) {
        String usuario = userContext.getUsername();

        service.generarMovimiento(request, usuario);

        return ResponseEntity.ok(ApiResponse.success(
                "Movimiento generado exitosamente.",
                null
        ));
    }
}
