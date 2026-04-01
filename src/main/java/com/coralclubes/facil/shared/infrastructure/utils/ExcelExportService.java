package com.coralclubes.facil.shared.infrastructure.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ExcelExportService {

    /**
     * Genera un archivo Excel a partir de una lista de datos en bruto.
     */
    public byte[] generarExcelBytes(List<Map<String, Object>> datos, String nombreHoja) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(sanitizarNombreHoja(nombreHoja));
            CellStyle headerStyle = crearEstiloHeader(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook);

            if (datos.isEmpty()) {
                Row row = sheet.createRow(0);
                Cell cell = row.createCell(0);
                cell.setCellValue("Sin datos para mostrar");
                workbook.write(out);
                return out.toByteArray();
            }

            // Obtener nombres de columnas del primer registro
            Map<String, Object> primeraFila = datos.getFirst();
            List<String> columnas = primeraFila.keySet().stream().toList();

            // Crear header
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Crear filas de datos
            for (int rowIndex = 0; rowIndex < datos.size(); rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                Map<String, Object> fila = datos.get(rowIndex);

                for (int colIndex = 0; colIndex < columnas.size(); colIndex++) {
                    Cell cell = row.createCell(colIndex);
                    Object valor = fila.get(columnas.get(colIndex));
                    escribirCeldaValor(cell, valor, dateStyle);
                }
            }

            // Auto-ajustar columnas
            for (int i = 0; i < columnas.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void escribirCeldaValor(Cell cell, Object valor, CellStyle dateStyle) {
        if (valor == null) {
            cell.setCellValue("");
        } else if (valor instanceof Number num) {
            cell.setCellValue(num.doubleValue());
        } else if (valor instanceof java.util.Date || valor instanceof java.sql.Date || valor instanceof java.sql.Timestamp) {
            cell.setCellValue(valor.toString());
            cell.setCellStyle(dateStyle);
        } else if (valor instanceof Boolean bool) {
            cell.setCellValue(bool);
        } else {
            cell.setCellValue(valor.toString());
        }
    }

    private CellStyle crearEstiloHeader(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle crearEstiloFecha(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper helper = workbook.getCreationHelper();
        style.setDataFormat(helper.createDataFormat().getFormat("dd/MM/yyyy HH:mm"));
        return style;
    }

    private String sanitizarNombreHoja(String nombre) {
        if (nombre == null || nombre.isBlank()) return "Reporte";
        String limpio = nombre.replaceAll("[\\\\/:*?\\[\\]]", "_");
        return limpio.length() > 31 ? limpio.substring(0, 31) : limpio;
    }

    public String generarNombreArchivo(String nombreReporte) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String limpio = nombreReporte.replaceAll("[^a-zA-Z0-9áéíóúñÁÉÍÓÚÑ ]", "").trim();
        return limpio + "_" + fecha + ".xlsx";
    }
}