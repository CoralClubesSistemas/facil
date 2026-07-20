package com.coralclubes.facil.modules.reportes.service;

import com.coralclubes.facil.modules.reportes.dto.response.ParametroMapeoDto;
import com.coralclubes.facil.modules.reportes.dto.response.ProcedimientoEjecucionDto;
import com.coralclubes.facil.modules.reportes.repository.ReportesMotorRepository;
import com.coralclubes.facil.shared.utils.ExcelExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportesByKeyService {

    private final ReportesMotorRepository repository;
    private final ExcelExportService excelExportService;

    /**
     * Genera el reporte asociado a una clave (Key) en formato Excel (.xlsx).
     *
     * @param key        Clave única del reporte.
     * @param parametros Parámetros de ejecución para el Stored Procedure.
     * @return Arreglo de bytes del archivo Excel generado.
     */
    public byte[] generarReporteByKey(String key, Map<String, Object> parametros) {
        log.info("Generando reporte por Key: {}, Params: {}", key, parametros);

        // 1. Obtener procedimiento por Key
        ProcedimientoEjecucionDto proc = repository.obtenerProcedimientoPorKey(key);
        if (proc == null) {
            throw new IllegalArgumentException("No se encontró el procedimiento de ejecución para la clave de reporte: " + key);
        }

        // 2. Obtener mapeo de parámetros
        List<ParametroMapeoDto> mapeo = repository.obtenerParametrosMapeo(proc.tipoReporte());

        // 3. Ejecutar el Stored Procedure mapeado
        List<Map<String, Object>> datos = repository.ejecutarReporteMapeado(proc.nombreStoredProcedure(), mapeo, parametros);

        // 4. Generar y devolver los bytes del archivo Excel
        try {
            return excelExportService.generarExcelBytes(datos, key);
        } catch (Exception e) {
            log.error("Error al generar Excel para el reporte con Key {}: {}", key, e.getMessage(), e);
            throw new RuntimeException("Error al generar el archivo Excel del reporte.", e);
        }
    }
}
