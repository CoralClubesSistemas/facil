package com.coralclubes.facil.modules.reportes.controller;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.reportes.dto.request.EjecutarReporteRequest;
import com.coralclubes.facil.modules.reportes.dto.response.*;
import com.coralclubes.facil.modules.reportes.enums.ClavesModulosReportes;
import com.coralclubes.facil.modules.reportes.service.ReportesAsyncService;
import com.coralclubes.facil.modules.reportes.service.ReportesCatalogoService;
import com.coralclubes.facil.modules.reportes.service.ReportesMotorService;
import com.coralclubes.facil.shared.infrastructure.domain.dto.ArchivoDescarga;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/reportes")
@RequiredArgsConstructor
public class ReportesController {

    private final ReportesMotorService motorService;
    private final ReportesCatalogoService catalogoService;
    private final UserContext userContext;
    private final ReportesAsyncService asyncService;
    private final StorageClient storageClient;

    // =========================================================================
    // REPORTES DISPONIBLES
    // =========================================================================

    @GetMapping("/activos")
    public ResponseEntity<ApiResponse<List<ReporteDisponibleDto>>> obtenerReportesActivos(
            @RequestParam Integer idRol,
            @RequestParam(required = false) ClavesModulosReportes modulo) {
        return ResponseEntity.ok(motorService.obtenerReportesActivos(idRol, modulo));
    }

    // =========================================================================
    // PARÁMETROS Y PROCEDIMIENTO
    // =========================================================================

    @GetMapping("/{idTipoReporte}/parametros")
    public ResponseEntity<ApiResponse<List<ParametroReporteDto>>> obtenerParametros(
            @PathVariable Integer idTipoReporte) {
        return ResponseEntity.ok(motorService.obtenerParametrosReporte(idTipoReporte));
    }

    @GetMapping("/{idTipoReporte}/mapeo")
    public ResponseEntity<ApiResponse<List<ParametroMapeoDto>>> obtenerParametrosMapeo(
            @PathVariable Integer idTipoReporte) {
        return ResponseEntity.ok(motorService.obtenerParametrosMapeo(idTipoReporte));
    }

    // =========================================================================
    // EJECUCIÓN SÍNCRONA (Para vista en pantalla - UI)
    // =========================================================================

    @PostMapping("/ejecutar")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> ejecutarReporte(
            @Valid @RequestBody EjecutarReporteRequest request) {
        return ResponseEntity.ok(motorService.ejecutarReporte(request));
    }

    // =========================================================================
    // EJECUCIÓN ASÍNCRONA (Para Exportación a Excel)
    // =========================================================================

    // 2. Modifica el endpoint:
    @PostMapping("/ejecutar/excel")
    public ResponseEntity<ApiResponse<Boolean>> solicitarExcelAsincrono(
            @Valid @RequestBody EjecutarReporteRequest request,
            @RequestParam(required = false, defaultValue = "Reporte") String nombreReporte) {

        // A. Motor Service hace su trabajo síncrono: Guarda la bitácora
        Integer idBitacora = motorService.registrarInicioReporteAsync(request, nombreReporte);
        String usuario = userContext.getUsername();

        // B. dispara el servicio asíncrono
        asyncService.procesarReporteYSubirAsincrono(request, nombreReporte, idBitacora, usuario);

        // C. Responde inmediatamente al Frontend
        return ResponseEntity.accepted().body(
                ApiResponse.success("Tu reporte se está generando en segundo plano. Te notificaremos cuando esté listo.", true)
        );
    }

    // =========================================================================
    // CATÁLOGOS
    // =========================================================================

    @GetMapping("/catalogos/{nombreCatalogo}")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerCatalogo(
            @PathVariable String nombreCatalogo) {
        return ResponseEntity.ok(catalogoService.obtenerCatalogo(nombreCatalogo));
    }

    // =========================================================================
    // PREFERENCIAS (Favoritos y Columnas guardadas por el usuario)
    // =========================================================================

    @GetMapping("/{idTipoReporte}/favoritos")
    public ResponseEntity<ApiResponse<List<FavoritoReporteDto>>> obtenerFavoritos(
            @PathVariable Integer idTipoReporte) {
        return ResponseEntity.ok(motorService.obtenerFavoritos(idTipoReporte));
    }

    @GetMapping("/{idTipoReporte}/columnas")
    public ResponseEntity<ApiResponse<List<ColumnaReporteDto>>> obtenerColumnas(
            @PathVariable Integer idTipoReporte) {
        return ResponseEntity.ok(motorService.obtenerColumnasReporte(idTipoReporte));
    }

    // =========================================================================
    // HISTORIAL Y BANDEJA DE DESCARGAS
    // =========================================================================

    @GetMapping("/historial")
    public ResponseEntity<ApiResponse<List<HistorialReporteDto>>> obtenerHistorial(
            @RequestParam ClavesModulosReportes clave
    ) {
        return ResponseEntity.ok(motorService.obtenerHistorialUsuario(clave));
    }

    @GetMapping("/descargar/{uuid}")
    public ResponseEntity<ApiResponse<ArchivoDescarga>> obtenerUrlDescarga(@PathVariable java.util.UUID uuid) {
        try {
            ArchivoDescarga urlDescarga = storageClient.obtenerUrlDescargaYNombre(uuid);
            return ResponseEntity.ok(ApiResponse.success("URL obtenida exitosamente", urlDescarga));
        } catch (Exception e) {
            throw new RuntimeException("No se pudo obtener la URL de descarga del archivo.");
        }
    }
}