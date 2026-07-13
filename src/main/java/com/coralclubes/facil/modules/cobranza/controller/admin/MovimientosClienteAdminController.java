package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.facil.modules.cobranza.dto.request.EstadoCuentaAdeudoRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.HistoricoMovimientosRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.EstadoCuentaAdeudoDto;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoHistoricoDto;
import com.coralclubes.facil.modules.cobranza.service.MovimientosClienteService;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.coralclubes.facil.modules.cobranza.dto.request.GenerarExcelHistoricoRequest;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/cobranza")
@RequiredArgsConstructor
public class MovimientosClienteAdminController {

    private final MovimientosClienteService service;
    private final UserContext context;

    @GetMapping("/estado-cuenta-adeudo")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<EstadoCuentaAdeudoDto>>> obtenerEstadoCuentaAdeudo(
            @RequestParam String membresia,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaCorte) {
        Integer idDesarrollo = context.getIdDesarrollo();

        EstadoCuentaAdeudoRequest request = EstadoCuentaAdeudoRequest.builder()
                .membresia(membresia)
                .fechaCorte(fechaCorte)
                .build();

        return ResponseEntity.ok(service.obtenerEstadoCuentaAdeudo(request, idDesarrollo));
    }

    @GetMapping("/historico-movimientos")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<MovimientoHistoricoDto>>> obtenerHistoricoMovimientos(
            @RequestParam String membresia,
            @RequestParam(required = false) List<String> tipoMovimientos,
            @RequestParam(required = false) Integer estatusMovimientos,
            @RequestParam(required = false) Integer desarrolloConsumo,
            @RequestParam(required = false) Integer idPadre,
            @RequestParam(required = false) Integer numeroRecibo,
            @RequestParam(required = false) Integer serieRecibo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaPago
    ) {

        HistoricoMovimientosRequest request = HistoricoMovimientosRequest.builder()
                .membresia(membresia)
                .tipoMovimientos(tipoMovimientos)
                .estatusMovimientos(estatusMovimientos)
                .desarrolloConsumo(desarrolloConsumo)
                .idPadre(idPadre)
                .numeroRecibo(numeroRecibo)
                .serieRecibo(serieRecibo)
                .fechaPago(fechaPago)
                .build();

        return ResponseEntity.ok(ApiResponse.success(
                "Histórico de movimientos obtenido con éxito.",
                service.obtenerHistoricoMovimientos(request)
        ));
    }

    @GetMapping("/estado-cuenta-pdf")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<byte[]> obtenerEstadoCuentaPdf(@RequestParam String membresia) {
        byte[] pdf = service.generarPdfEstadoCuentaSocio(membresia);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=estado-cuenta-" + membresia + ".pdf")
                .body(pdf);
    }

    @GetMapping("/estado-cuenta-historico-pdf")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<byte[]> obtenerEstadoCuentaHistoricoPdf(
            @RequestParam String membresia,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaCorte) {
        byte[] pdf = service.generarPdfEstadoCuentaHistoricoSocio(membresia, fechaCorte);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=estado-cuenta-historico-" + membresia + ".pdf")
                .body(pdf);
    }

    @GetMapping("/historico-movimientos/excel/columnas")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<String>>> obtenerColumnasExcel(@RequestParam String membresia) {
        return ResponseEntity.ok(ApiResponse.success(
                "Listado de columnas de Excel obtenido con éxito.",
                service.obtenerColumnasExcel(membresia)
        ));
    }

    @PostMapping("/historico-movimientos/excel")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<byte[]> descargarExcelHistorico(@RequestBody GenerarExcelHistoricoRequest request) throws IOException {
        byte[] excelBytes = service.generarExcelHistorico(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=historico-movimientos-" + request.membresia() + ".xlsx")
                .body(excelBytes);
    }
}
