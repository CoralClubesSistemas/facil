package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.facil.modules.cobranza.dto.request.ProcesarPagoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.EstadoCumplimientoDto;
import com.coralclubes.facil.modules.cobranza.dto.response.ProcesarPagoResponse;
import com.coralclubes.facil.modules.cobranza.service.IntentoPagoService;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/cobranza/ordenes/{uuid}/pagos")
@RequiredArgsConstructor
public class IntentoPagoController {
    private final IntentoPagoService service;

    @PostMapping
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<ProcesarPagoResponse>> procesarIntentoPago(
            @PathVariable UUID uuid,
            @Valid @RequestBody ProcesarPagoRequest request
    ) {
        return ResponseEntity.ok(service.iniciarPago(uuid, request));
    }

    // Endpoint para consultar el estado actual de los pagos de una orden
    @GetMapping
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<EstadoCumplimientoDto>> obtenerEstadoPagos(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.evaluarCumplimientoDeOrden(uuid));
    }

    @DeleteMapping("/{idPago}")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<EstadoCumplimientoDto>> eliminarIntentoPago(
            @PathVariable UUID uuid,
            @PathVariable Integer idPago
    ) {
        return ResponseEntity.ok(service.eliminarPago(uuid, idPago));
    }
}
