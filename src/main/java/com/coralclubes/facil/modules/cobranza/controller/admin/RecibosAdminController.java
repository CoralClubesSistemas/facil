package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.facil.modules.cobranza.dto.response.BuscarRecibosResponse;
import com.coralclubes.facil.modules.cobranza.dto.request.CancelarReciboRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.RegistarEvidenciaReciboCancelado;
import com.coralclubes.facil.modules.cobranza.dto.response.ObtenerDetallesReciboResponse;
import com.coralclubes.facil.modules.cobranza.service.RecibosService;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitarUrlRequest;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaDto;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/recibos")
@RequiredArgsConstructor
public class RecibosAdminController {

    private final RecibosService recibosService;

    @GetMapping("/buscar")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<BuscarRecibosResponse>>> buscarRecibos(
            @RequestParam(required = false) String folioRecibo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaGeneracionDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaGeneracionA,
            @RequestParam(required = false) String membresia,
            @RequestParam(required = false) Integer desarrolloId,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String nombreSocio,
            @RequestParam(required = false) String terminacionTarjeta,
            @RequestParam(defaultValue = "false") Boolean filtrarPorEstatus
    ) {
        return ResponseEntity.ok(recibosService.buscarRecibos(
                folioRecibo,
                fechaGeneracionDe,
                fechaGeneracionA,
                membresia,
                desarrolloId,
                usuario,
                nombreSocio,
                terminacionTarjeta,
                filtrarPorEstatus
        ));
    }

    @GetMapping("/detalles")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<ObtenerDetallesReciboResponse>> obtenerDetallesRecibo(
            @RequestParam Integer numeroRecibo,
            @RequestParam Integer serieReciboId,
            @RequestParam String membresia
    ) {
        return ResponseEntity.ok(recibosService.obtenerDetallesRecibo(numeroRecibo, serieReciboId, membresia));
    }

    @PostMapping("/cancelados/recibo")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<Boolean>> cancelarRecibo(
            @Valid @RequestBody CancelarReciboRequest request
    ) {
        return ResponseEntity.ok(recibosService.cancelarRecibo(request));
    }

    @PostMapping("/cancelados/evidencia/urls")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<RespuestaCargaDto>>> solicitarUrlsDeCarga(
            @Valid @RequestBody List<SolicitarUrlRequest> solicitudes
    ) {
        return ResponseEntity.ok(recibosService.solicitarUrlsDeCarga(solicitudes));
    }

    @PostMapping("/cancelados/evidencia")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<Boolean>> registrarEvidenciaReciboCancelado(
            @Valid @RequestBody RegistarEvidenciaReciboCancelado request
    ) {
        return ResponseEntity.ok(recibosService.registrarEvidenciaReciboCancelado(request));
    }
}

