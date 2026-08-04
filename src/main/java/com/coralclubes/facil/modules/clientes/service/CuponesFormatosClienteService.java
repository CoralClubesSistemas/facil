package com.coralclubes.facil.modules.clientes.service;

import com.coralclubes.facil.modules.clientes.dto.response.CuponFormatoInfoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponImagenFormatoResponse;
import com.coralclubes.facil.modules.cobranza.service.CuponesService;
import com.coralclubes.facil.shared.infrastructure.pdf.service.GotenbergClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Servicio encargado de la generación de documentos PDF para formatos de cupones de cliente.
 * <p>
 * Este servicio realiza el procesamiento dinámico en memoria de plantillas de cupones:
 * 1. Obtiene la información del cupón asignado a la membresía mediante Stored Procedure.
 * 2. Recupera la plantilla base (imagen) y su configuración de posiciones porcentuales guardadas.
 * 3. Dibuja las variables dinámicas (nombre, folio, membresía, etc.) directamente sobre los píxeles de la imagen en memoria usando Java 2D Graphics.
 * 4. Compone una cuadrícula de 10 cupones por página (2 columnas x 5 filas) en formato Carta (Letter) en HTML.
 * 5. Renderiza el PDF final utilizando el cliente de Gotenberg sin persistir ningún archivo temporal en disco.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CuponesFormatosClienteService {

    private final CuponesMembresiasService cuponesMembresiasService;
    private final CuponesService cuponesService;
    private final GotenbergClient gotenbergClient;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    /**
     * DTO interno para mapear la configuración JSON de posicionamiento de variables.
     */
    @Data
    private static class VariableConfigDto {
        private String id;
        private String key;
        private String label;

        @com.fasterxml.jackson.annotation.JsonProperty("xPercent")
        private Double xPercent;

        @com.fasterxml.jackson.annotation.JsonProperty("yPercent")
        private Double yPercent;
    }

    /**
     * Genera un archivo PDF con la planilla de cupones formateados y estampados para una asignación específica de membresía.
     *
     * @param idCuponPqa Identificador del paquete de cupones asignado a la membresía (PQAC_ID).
     * @return Arreglo de bytes (`byte[]`) correspondiente al PDF generado.
     */
    public byte[] generarPdfFormatosCupon(Integer idCuponPqa) {
        log.info("Iniciando generación de PDF de formatos para cuponPqaId: {}", idCuponPqa);

        // 1. Obtener el listado de información del cupón asignado a la membresía
        List<CuponFormatoInfoResponse> infoList = cuponesMembresiasService.obtenerInfoFormatosCupones(idCuponPqa);
        if (infoList.isEmpty()) {
            throw new RuntimeException("No se encontró información para el cupón especificado ID: " + idCuponPqa);
        }

        Integer cuponId = infoList.getFirst().cuponId();
        if (cuponId == null) {
            throw new RuntimeException("El cuponId no fue devuelto en la información del cupón.");
        }

        // 2. Obtener la plantilla de imagen configurada en el módulo de cobranza
        CuponImagenFormatoResponse formatoResponse = cuponesService.obtenerImagenCupon(cuponId);
        if (formatoResponse == null || formatoResponse.archivoDescarga() == null || formatoResponse.archivoDescarga().urlDescarga() == null) {
            throw new RuntimeException("No se encontró la plantilla de imagen configurada para el cupón ID: " + cuponId);
        }

        // Descargar la imagen base a memoria
        byte[] imagenBaseBytes = descargarImagen(formatoResponse.archivoDescarga().urlDescarga());

        // Parsear la configuración de variables
        List<VariableConfigDto> variablesConfig = parseConfiguracionJson(formatoResponse.configuracionJson());

        // 3. Estampar la información de cada registro sobre la imagen en memoria
        List<String> imagenesBase64List = new ArrayList<>();
        for (CuponFormatoInfoResponse info : infoList) {
            byte[] imagenModificadaBytes = estamparVariablesEnImagen(imagenBaseBytes, info, variablesConfig);
            String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(imagenModificadaBytes);
            imagenesBase64List.add(base64Image);
        }

        // 4. Maquetar en cuadrícula HTML y convertir a PDF mediante Gotenberg
        String htmlContent = construirHtmlParaPdf(imagenesBase64List);
        byte[] pdfBytes = gotenbergClient.convertHtmlToPdf(htmlContent);

        log.info("PDF de formatos de cupones generado exitosamente para cuponPqaId: {} (Total cupones: {})", idCuponPqa, infoList.size());
        return pdfBytes;
    }

    /**
     * Descarga la imagen base desde la URL firmada directamente a memoria.
     */
    private byte[] descargarImagen(String url) {
        try {
            return restClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .body(byte[].class);
        } catch (Exception e) {
            log.error("Error al descargar la imagen base del cupón desde la URL firmada", e);
            throw new RuntimeException("Error al obtener la imagen base del cupón", e);
        }
    }

    /**
     * Deserializa la cadena JSON de variables a la lista de objetos de configuración.
     */
    private List<VariableConfigDto> parseConfiguracionJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            ObjectMapper mapper = objectMapper.copy()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(json, new TypeReference<List<VariableConfigDto>>() {
            });
        } catch (Exception e) {
            log.error("Error al deserializar la configuración JSON de variables del formato de cupón", e);
            return List.of();
        }
    }

    /**
     * Dibuja los valores dinámicos de cada cupón sobre la imagen base en memoria usando Java 2D Graphics.
     */
    private byte[] estamparVariablesEnImagen(byte[] imagenBaseBytes, CuponFormatoInfoResponse info, List<VariableConfigDto> variablesConfig) {
        try (InputStream is = new ByteArrayInputStream(imagenBaseBytes)) {
            BufferedImage originalImage = ImageIO.read(is);
            if (originalImage == null) {
                throw new RuntimeException("No se pudo leer el formato binario de la imagen base");
            }

            BufferedImage image = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D gInit = image.createGraphics();
            gInit.drawImage(originalImage, 0, 0, null);
            gInit.dispose();

            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int imgWidth = image.getWidth();
            int imgHeight = image.getHeight();

            // Tamaño de fuente proporcional a la resolución de la imagen base (~1.8% del ancho)
            int calculatedFontSize = Math.max(14, (int) (imgWidth * 0.018));
            Font font = new Font("SansSerif", Font.BOLD, calculatedFontSize);
            g2d.setFont(font);
            g2d.setColor(Color.BLACK);

            FontMetrics fm = g2d.getFontMetrics(font);
            int textAscent = fm.getAscent();

            for (VariableConfigDto varConfig : variablesConfig) {
                String valor = resolverValorVariable(varConfig.getKey(), info);
                if (valor == null || valor.isBlank()) {
                    continue;
                }

                // Calcular posición X e Y según porcentajes
                double xPercent = varConfig.getXPercent() != null ? varConfig.getXPercent() : 0.0;
                double yPercent = varConfig.getYPercent() != null ? varConfig.getYPercent() : 0.0;

                int x = (int) Math.round((xPercent / 100.0) * imgWidth);
                // Se suma textAscent para convertir la posición superior (top) de la interfaz al baseline de Java Graphics2D
                int y = (int) Math.round((yPercent / 100.0) * imgHeight) + textAscent;

                g2d.drawString(valor, x, y);
            }

            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al estampar las variables en la imagen del cupón en memoria", e);
            throw new RuntimeException("Error al procesar la imagen del cupón en memoria", e);
        }
    }

    /**
     * Asocia la clave de la variable configurada en la plantilla con la propiedad correspondiente del registro del cupón.
     */
    private String resolverValorVariable(String key, CuponFormatoInfoResponse info) {
        if (key == null) return "";
        return switch (key.toLowerCase().trim()) {
            case "nombre" -> info.nombre() != null ? info.nombre() : "";
            case "folio" -> info.folio() != null ? info.folio() : "";
            case "num_membresia", "membresia" -> info.membresia() != null ? info.membresia() : "";
            default -> "";
        };
    }

    /**
     * Construye el documento HTML con la maquetación en tabla fija de 10 cupones por hoja tamaño Carta (Letter).
     */
    private String construirHtmlParaPdf(List<String> imagenesBase64List) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<meta charset=\"UTF-8\" />\n");
        html.append("<style>\n");
        html.append("  @page { size: letter portrait; margin: 5mm; }\n");
        html.append("  * { box-sizing: border-box; }\n");
        html.append("  body { margin: 0; padding: 0; background-color: #ffffff; font-family: sans-serif; }\n");
        html.append("  .page {\n");
        html.append("    page-break-after: always;\n");
        html.append("    width: 100%;\n");
        html.append("    height: 100%;\n");
        html.append("    display: table;\n");
        html.append("    table-layout: fixed;\n");
        html.append("    border-collapse: collapse;\n");
        html.append("  }\n");
        html.append("  .page:last-child { page-break-after: avoid; }\n");
        html.append("  .row { display: table-row; height: 19.5vh; }\n");
        html.append("  .cell {\n");
        html.append("    display: table-cell;\n");
        html.append("    width: 50%;\n");
        html.append("    height: 19.5vh;\n");
        html.append("    vertical-align: middle;\n");
        html.append("    text-align: center;\n");
        html.append("    padding: 2px;\n");
        html.append("  }\n");
        html.append("  .cell img {\n");
        html.append("    max-width: 100%;\n");
        html.append("    max-height: 100%;\n");
        html.append("    object-fit: contain;\n");
        html.append("    display: block;\n");
        html.append("    margin: 0 auto;\n");
        html.append("  }\n");
        html.append("</style>\n</head>\n<body>\n");

        int totalCupones = imagenesBase64List.size();
        int cuponesPorPagina = 10;

        for (int i = 0; i < totalCupones; i += cuponesPorPagina) {
            html.append("  <div class=\"page\">\n");

            List<String> lotePagina = imagenesBase64List.subList(i, Math.min(i + cuponesPorPagina, totalCupones));

            for (int r = 0; r < 5; r++) {
                html.append("    <div class=\"row\">\n");
                for (int c = 0; c < 2; c++) {
                    int index = r * 2 + c;
                    html.append("      <div class=\"cell\">\n");
                    if (index < lotePagina.size()) {
                        html.append("        <img src=\"").append(lotePagina.get(index)).append("\" />\n");
                    }
                    html.append("      </div>\n");
                }
                html.append("    </div>\n");
            }

            html.append("  </div>\n");
        }

        html.append("</body>\n</html>");
        return html.toString();
    }
}

