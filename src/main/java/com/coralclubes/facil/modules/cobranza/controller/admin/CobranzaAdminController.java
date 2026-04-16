package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.ConsultarOrdenCobranzaResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.FormaPagoDto;
import com.coralclubes.facil.modules.cobranza.dto.response.GenerarOrdenCobranzaResponse;
import com.coralclubes.facil.modules.cobranza.service.CobranzaService;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/catalogos/formas-pago")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<FormaPagoDto>>> obtenerFormasDePago() {
        return ResponseEntity.ok(cobranzaService.obtenerFormasDePago());
    }

    @PostMapping("/ordenes/{uuid}/finalizar-recibo")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<String>> finalizarOrdenYGenerarRecibo(
            @PathVariable UUID uuid,
            @RequestParam Integer tipoSerieRecibo,
            @RequestParam String correo
    ) {
        String username = userContext.getUsername();
        return ResponseEntity.ok(cobranzaService.finalizarOrdenYGenerarRecibo(uuid.toString(), tipoSerieRecibo, username, correo));
    }
}
