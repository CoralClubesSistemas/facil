package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.facil.modules.reservaciones.dto.request.DatosCartaOcupacionDto;
import com.coralclubes.facil.shared.infrastructure.pdf.service.PdfGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeneradorDocumentosService {

    private final PdfGeneratorService pdfGenerator;

    public record DocumentoCartaOcupacion(byte[] pdfBytes, String nombreArchivo) {}

    /**
     * Genera la Carta de Ocupación en PDF (retorna los bytes y el nombre del archivo).
     */
    public DocumentoCartaOcupacion generarCartaOcupacion(DatosCartaOcupacionDto datos) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("fechaEmision", datos.fechaEmision());
        variables.put("titular", datos.titular());
        variables.put("membresia", datos.membresia() != null ? datos.membresia() : "PÚBLICO GENERAL");
        variables.put("foliosReservacion", datos.foliosReservacion());
        variables.put("habitaciones", datos.habitaciones());
        variables.put("observaciones", datos.observaciones() != null ? datos.observaciones() : "Sin observaciones adicionales.");
        java.text.DecimalFormat df = new java.text.DecimalFormat("$#,##0.00");
        variables.put("importeTotal", df.format(datos.importeTotal()));
        variables.put("fechaEntrada", datos.fechaEntrada());
        variables.put("fechaSalida", datos.fechaSalida());
        variables.put("desarrollo", datos.desarrollo());

        // Convierte el HTML a bytes
        byte[] pdfBytes = pdfGenerator.generarPdfDesdeHtml("CARTA_OCUPACION", variables);

        // Limpiamos los folios para que el nombre del archivo no tenga espacios
        String foliosLimpio = datos.foliosReservacion().replace(" ", "").replace(",", "_");
        String nombreArchivo = "CARTA_OCUPACION_" + foliosLimpio + ".pdf";

        return new DocumentoCartaOcupacion(pdfBytes, nombreArchivo);
    }
}