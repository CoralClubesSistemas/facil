package com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.service;

import com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.client.FilesAnalysisClient;
import com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.dto.AnalisisArchivoSolicitud;
import com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.dto.ResultadoAnalisis;
import com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.extractor.BedrockVisionExtractor;
import com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.extractor.DigitalPdfExtractor;
import com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.extractor.PdfToImageConverter;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.InfoArchivoDto;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.clients.files-analysis.provider", havingValue = "local", matchIfMissing = true)
public class FilesAnalysisService implements FilesAnalysisClient {

    private final StorageClient storageClient;
    private final DigitalPdfExtractor digitalPdfExtractor;
    private final PdfToImageConverter pdfToImageConverter;
    private final BedrockVisionExtractor bedrockVisionExtractor;
    private final BusinessLogger businessLogger;

    @Override
    public <T> ResultadoAnalisis<T> analizar(AnalisisArchivoSolicitud<T> solicitud) {
        log.info("Iniciando análisis de archivo con ID {}", solicitud.fileId());

        // 1. Consultar metadatos validados en Coral Almacenamiento
        InfoArchivoDto infoArchivo = storageClient.consultarArchivo(solicitud.fileId());
        String contentType = infoArchivo.contentType() != null ? infoArchivo.contentType().toLowerCase().trim() : "";

        // 2. Descargar bytes del archivo
        byte[] bytesArchivo = storageClient.descargarArchivo(infoArchivo.urlDescarga());

        // 3. Procesar según contentType
        if (contentType.equals("application/pdf")) {
            return procesarPdf(solicitud, bytesArchivo);
        }

        if (contentType.startsWith("image/")) {
            return procesarImagen(solicitud, bytesArchivo, contentType);
        }

        throw new IllegalArgumentException("El tipo de archivo '" + contentType + "' no es soportado para análisis.");
    }

    private <T> ResultadoAnalisis<T> procesarPdf(AnalisisArchivoSolicitud<T> solicitud, byte[] bytesArchivo) {
        // Paso 1: Intentar extracción directa de PDF digital
        Optional<String> textoOpt = digitalPdfExtractor.extraerTexto(bytesArchivo);

        if (textoOpt.isPresent() && solicitud.extractorTextoDigital() != null) {
            Optional<T> resultadoDirectoOpt = solicitud.extractorTextoDigital().apply(textoOpt.get());
            if (resultadoDirectoOpt.isPresent()) {
                businessLogger.info("SYSTEM", "Extracción directa completada exitosamente para PDF: {}", solicitud.fileId());
                return ResultadoAnalisis.digital(resultadoDirectoOpt.get());
            }
            log.info("La extracción directa digital del PDF no satisfizo los campos requeridos. Activando fallback a Bedrock.");
        }

        // Paso 2: Si es un PDF escaneado (sin texto) o la extracción digital falló/incompleta, renderizar a imagen y enviar a Bedrock
        Optional<byte[]> imagenRenderizada = pdfToImageConverter.convertirPrimeraPaginaAImagen(bytesArchivo);
        if (imagenRenderizada.isPresent()) {
            businessLogger.info("SYSTEM", "Invocando visión multimodal Bedrock para PDF escaneado/fallback: {}", solicitud.fileId());
            T resultadoAi = bedrockVisionExtractor.extraerDesdeImagen(
                    imagenRenderizada.get(),
                    "image/png",
                    solicitud.tipoDestino(),
                    solicitud.promptInstrucciones()
            );
            return ResultadoAnalisis.ai(resultadoAi);
        }

        // Paso 3: Fallback secundario si falló el renderizado pero había texto plano
        if (textoOpt.isPresent()) {
            businessLogger.info("SYSTEM", "Invocando Bedrock sobre texto extraído del PDF: {}", solicitud.fileId());
            T resultadoAi = bedrockVisionExtractor.extraerDesdeTexto(
                    textoOpt.get(),
                    solicitud.tipoDestino(),
                    solicitud.promptInstrucciones()
            );
            return ResultadoAnalisis.ai(resultadoAi);
        }

        throw new IllegalStateException("No fue posible extraer información ni renderizar imagen del PDF con ID: " + solicitud.fileId());
    }

    private <T> ResultadoAnalisis<T> procesarImagen(AnalisisArchivoSolicitud<T> solicitud, byte[] bytesArchivo, String contentType) {
        businessLogger.info("SYSTEM", "Invocando visión multimodal Bedrock para imagen: {}", solicitud.fileId());
        T resultadoAi = bedrockVisionExtractor.extraerDesdeImagen(
                bytesArchivo,
                contentType,
                solicitud.tipoDestino(),
                solicitud.promptInstrucciones()
        );
        return ResultadoAnalisis.ai(resultadoAi);
    }
}
