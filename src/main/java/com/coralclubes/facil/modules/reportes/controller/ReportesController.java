package com.coralclubes.facil.modules.reportes.controller;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.reportes.dto.request.EjecutarReporteRequest;
import com.coralclubes.facil.modules.reportes.dto.response.*;
import com.coralclubes.facil.modules.reportes.enums.ClavesModulosReportes;
import com.coralclubes.facil.modules.reportes.service.ExcelExportService;
import com.coralclubes.facil.modules.reportes.service.ReportesCatalogoService;
import com.coralclubes.facil.modules.reportes.service.ReportesMotorService;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/reportes")
@RequiredArgsConstructor
public class ReportesController {

    private final ReportesMotorService motorService;
    private final ReportesCatalogoService catalogoService;
    private final ExcelExportService excelExportService;

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
    // EJECUCIÓN (Persiste favoritos y columnas automáticamente)
    // =========================================================================

    @PostMapping("/ejecutar")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> ejecutarReporte(
            @Valid @RequestBody EjecutarReporteRequest request) {
        return ResponseEntity.ok(motorService.ejecutarReporte(request));
    }

    @PostMapping("/ejecutar/excel")
    public ResponseEntity<byte[]> ejecutarYExportarExcel(
            @Valid @RequestBody EjecutarReporteRequest request,
            @RequestParam(required = false, defaultValue = "Reporte") String nombreReporte) {
        try {
            byte[] excel = excelExportService.generarExcel(request, nombreReporte);
            String nombreArchivo = excelExportService.generarNombreArchivo(nombreReporte);
            String encoded = URLEncoder.encode(nombreArchivo, StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excel);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el Excel: " + e.getMessage(), e);
        }
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
}