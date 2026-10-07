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
     * Renderiza la primera página de un documento PDF a imagen PNG en memoria (página 1).
     *
     * @param pdfBytes Bytes del PDF.
     * @return Arreglo de bytes de la imagen PNG generada, o Optional.empty() si falla.
     */
    public Optional<byte[]> convertirPrimeraPaginaAImagen(byte[] pdfBytes) {
        return convertirPaginaAImagen(pdfBytes, 1);
    }

    /**
     * Renderiza una página específica (1-indexed) de un documento PDF a imagen PNG en memoria.
     * Si la página solicitada excede el rango del documento o es menor a 1, utiliza por defecto la primera página.
     *
     * @param pdfBytes Bytes del PDF.
     * @param numeroPagina Número de página (1-indexed). Si es null o <= 0 se asume 1.
     * @return Arreglo de bytes de la imagen PNG generada, o Optional.empty() si falla.
     */
    public Optional<byte[]> convertirPaginaAImagen(byte[] pdfBytes, Integer numeroPagina) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            return Optional.empty();
        }

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            int totalPaginas = document.getNumberOfPages();
            if (totalPaginas == 0) {
                return Optional.empty();
            }

            int paginaDeseada = (numeroPagina != null && numeroPagina > 0) ? numeroPagina : 1;
            // 0-indexed para PDFBox:
            int pageIndex = paginaDeseada - 1;

            if (pageIndex >= totalPaginas) {
                log.warn("Página solicitada {} fuera de rango (total de páginas: {}). Usando primera página.",
                        paginaDeseada, totalPaginas);
                pageIndex = 0;
            }

            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(pageIndex, DEFAULT_DPI, ImageType.RGB);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return Optional.of(baos.toByteArray());
        } catch (Exception e) {
            log.error("Error al convertir PDF a imagen para análisis de visión: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}
