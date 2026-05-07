package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.*;
import com.coralclubes.facil.modules.cobranza.service.CobranzaService;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/cobranza")
@RequiredArgsConstructor
public class CobranzaAdminController {

    private final CobranzaService cobranzaService;
    private final UserContext userContext;

    @PostMapping("/ordenes/generar")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<GenerarOrdenCobranzaResponse>> generarOrdenCobranza(
            @Valid @RequestBody GenerarOrdenCobranzaRequest request
    ) {
        String username = userContext.getUsername();

        return ResponseEntity.ok(cobranzaService.generarOrdenCobranza(request, username));
    }

    @GetMapping("/ordenes/{uuid}")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<ConsultarOrdenCobranzaResponse>> obtenerOrdenCobranza(
            @PathVariable UUID uuid
    ) {
        return ResponseEntity.ok(cobranzaService.consultarOrdenCobranza(uuid));
    }

    @GetMapping("/ordenes/recuperar-uuid")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<RecuperarOrdenCobranzaResponse>> recuperarOrdenCobranza(
            @RequestParam Integer movimientoId,
            @RequestParam String membresia
    ) {
        return ResponseEntity.ok(cobranzaService.recuperarOrdenCobranza(movimientoId, membresia));
    }

    @GetMapping("/catalogos/formas-pago")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<FormaPagoDto>>> obtenerFormasDePago() {
        return ResponseEntity.ok(cobranzaService.obtenerFormasDePago());
    }

    @GetMapping("/depositos")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<DepositoCobranzaDto>>> obtenerDepositos(
            @RequestParam Integer idBanco,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDeposito,
            @RequestParam(defaultValue = "") String busqueda,
            @RequestParam BigDecimal monto
            ) {
        return ResponseEntity.ok(cobranzaService.obtenerDepositos(idBanco, fechaDeposito, busqueda, monto));
    }

    @PostMapping("/ordenes/{uuid}/finalizar-recibo")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<FinalizarOrdenCobranzaResponse>> finalizarOrdenYGenerarRecibo(
            @PathVariable UUID uuid,
            @RequestParam Integer tipoSerieRecibo,
            @RequestParam List<String> correos
    ) {
        String username = userContext.getUsername();
        return ResponseEntity.ok(cobranzaService.finalizarOrdenYGenerarRecibo(uuid.toString(), tipoSerieRecibo, username, correos));
    }

    @DeleteMapping("/ordenes/{uuid}")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<Void>> cancelarOrdenCobranza(
            @PathVariable String uuid) {
        cobranzaService.cancelarOrdenCobranzaSinPago(uuid);
        return ResponseEntity.ok(ApiResponse.success("Orden de cobranza cancelada correctamente.", null));
    }

    @GetMapping("/recibos/cancelados")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<RecibosCancelados>>> obtenerRecibosCancelados(
            @RequestParam String membresia,
            @RequestParam(required = false) String recibo) {
        return ResponseEntity.ok(cobranzaService.obtenerRecibosCancelados(membresia, recibo));
    }

    @GetMapping("/cartera-ejecutivo")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<CarteraEjecutivoResponse>>> obtenerCarteraEjecutivo() {
        return ResponseEntity.ok(cobranzaService.obtenerCarteraEjecutivo());
    }

    @GetMapping("/analisis-ia/{membresia}")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<AnalisisCobranzaResponse>> analizarClienteConIa(
            @PathVariable String membresia
    ) {
        return ResponseEntity.ok(cobranzaService.analizarClienteParaCobranza(membresia));
    }
}
