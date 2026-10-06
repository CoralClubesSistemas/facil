package com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.extractor;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Optional;

@Slf4j
@Component
public class PdfToImageConverter {

    private static final int DEFAULT_DPI = 200;

    /**
     * Renderiza la primera página de un documento PDF a imagen PNG en memoria.
     *
     * @param pdfBytes Bytes del PDF.
     * @return Arreglo de bytes de la imagen PNG generada, o Optional.empty() si falla.
     */
    public Optional<byte[]> convertirPrimeraPaginaAImagen(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            return Optional.empty();
        }

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (document.getNumberOfPages() == 0) {
                return Optional.empty();
            }

            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, DEFAULT_DPI, ImageType.RGB);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return Optional.of(baos.toByteArray());
        } catch (Exception e) {
            log.error("Error al convertir PDF a imagen para análisis de visión: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}
