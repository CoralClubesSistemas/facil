package com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.extractor;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class DigitalPdfExtractor {

    /**
     * Extrae texto plano de un documento PDF digital completo.
     *
     * @param pdfBytes Bytes del PDF.
     * @return Optional con el texto extraído si contiene texto legible, o Optional.empty() si está vacío o falla.
     */
    public Optional<String> extraerTexto(byte[] pdfBytes) {
        return extraerTexto(pdfBytes, null);
    }

    /**
     * Extrae texto plano de una página específica o del documento completo.
     *
     * @param pdfBytes Bytes del PDF.
     * @param numeroPagina Número de página (1-indexed). Si es null, extrae todo el documento.
     * @return Optional con el texto extraído si contiene texto legible, o Optional.empty() si está vacío o falla.
     */
    public Optional<String> extraerTexto(byte[] pdfBytes, Integer numeroPagina) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            return Optional.empty();
        }

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            if (numeroPagina != null && numeroPagina > 0 && numeroPagina <= document.getNumberOfPages()) {
                stripper.setStartPage(numeroPagina);
                stripper.setEndPage(numeroPagina);
            }

            String texto = stripper.getText(document);

            if (texto != null && !texto.trim().isBlank()) {
                return Optional.of(texto.trim());
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("No fue posible extraer texto digital del PDF: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
