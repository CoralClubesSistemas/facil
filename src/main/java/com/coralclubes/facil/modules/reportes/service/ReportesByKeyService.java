package com.coralclubes.facil.modules.reportes.service;

import com.coralclubes.facil.modules.reportes.dto.response.ColumnaMetadataDto;
import com.coralclubes.facil.modules.reportes.dto.response.ParametroMapeoDto;
import com.coralclubes.facil.modules.reportes.dto.response.ProcedimientoEjecucionDto;
import com.coralclubes.facil.modules.reportes.repository.ReportesMotorRepository;
import com.coralclubes.facil.shared.utils.ExcelExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportesByKeyService {

    private final ReportesMotorRepository repository;
    private final ExcelExportService excelExportService;

    /**
     * Obtiene las columnas configuradas o devueltas por el SP asociado a una clave (Key).
     *
     * @param key Clave única del reporte.
     * @return Lista con los nombres de las columnas.
     */
    public List<String> obtenerColumnasByKey(String key) {
        log.info("Obteniendo columnas para la Key: {}", key);

        ProcedimientoEjecucionDto proc = repository.obtenerProcedimientoPorKey(key);
        if (proc == null) {
            throw new IllegalArgumentException("No se encontró el procedimiento de ejecución para la clave de reporte: " + key);
        }

        List<ParametroMapeoDto> mapeo = repository.obtenerParametrosMapeo(proc.tipoReporte());
        List<ColumnaMetadataDto> metadata = repository.obtenerMetadataColumnas(proc.nombreStoredProcedure(), mapeo);

        return metadata.stream()
                .map(ColumnaMetadataDto::nombreColumnaDB)
                .toList();
    }

    /**
     * Genera el reporte asociado a una clave (Key) en formato Excel (.xlsx).
     *
     * @param key        Clave única del reporte.
     * @param parametros Parámetros de ejecución para el Stored Procedure.
     * @return Arreglo de bytes del archivo Excel generado.
     */
    public byte[] generarReporteByKey(String key, Map<String, Object> parametros) {
        return generarReporteByKey(key, parametros, null);
    }

    /**
     * Genera el reporte asociado a una clave (Key) en formato Excel (.xlsx) filtrando opcionalmente las columnas.
     *
     * @param key        Clave única del reporte.
     * @param parametros Parámetros de ejecución para el Stored Procedure.
     * @param columnas   Columnas a incluir en el reporte (opcional).
     * @return Arreglo de bytes del archivo Excel generado.
     */
    public byte[] generarReporteByKey(String key, Map<String, Object> parametros, List<String> columnas) {
        log.info("Generando reporte por Key: {}, Params: {}, Columnas: {}", key, parametros, columnas);

        // 1. Obtener procedimiento por Key
        ProcedimientoEjecucionDto proc = repository.obtenerProcedimientoPorKey(key);
        if (proc == null) {
            throw new IllegalArgumentException("No se encontró el procedimiento de ejecución para la clave de reporte: " + key);
        }

        // 2. Obtener mapeo de parámetros
        List<ParametroMapeoDto> mapeo = repository.obtenerParametrosMapeo(proc.tipoReporte());

        // 3. Ejecutar el Stored Procedure mapeado
        List<Map<String, Object>> datos = repository.ejecutarReporteMapeado(proc.nombreStoredProcedure(), mapeo, parametros);

        // 4. Filtrar y reordenar columnas si fueron especificadas
        List<Map<String, Object>> datosAExportar = datos;
        if (columnas != null && !columnas.isEmpty() && !datos.isEmpty()) {
            datosAExportar = datos.stream()
                    .map(row -> {
                        Map<String, Object> filteredRow = new LinkedHashMap<>();
                        for (String col : columnas) {
                            String matchCol = row.keySet().stream()
                                    .filter(k -> k.equalsIgnoreCase(col))
                                    .findFirst()
                                    .orElse(null);
                            filteredRow.put(col, matchCol != null ? row.get(matchCol) : "");
                        }
                        return filteredRow;
                    })
                    .toList();
        }

        // 5. Generar y devolver los bytes del archivo Excel
        try {
            return excelExportService.generarExcelBytes(datosAExportar, key);
        } catch (Exception e) {
            log.error("Error al generar Excel para el reporte con Key {}: {}", key, e.getMessage(), e);
            throw new RuntimeException("Error al generar el archivo Excel del reporte.", e);
        }
    }
}
