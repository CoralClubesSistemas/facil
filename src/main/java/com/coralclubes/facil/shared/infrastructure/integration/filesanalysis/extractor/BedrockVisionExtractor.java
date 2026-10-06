package com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.extractor;

import com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.config.FilesAnalysisProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;

import java.lang.reflect.RecordComponent;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class BedrockVisionExtractor {

    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final FilesAnalysisProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * Extrae información estructurada a partir de una imagen utilizando AWS Bedrock Converse API.
     *
     * @param imagenBytes         Bytes de la imagen.
     * @param contentType         Tipo MIME (image/png, image/jpeg, etc.).
     * @param tipoDestino         Clase destino (Record o DTO) donde deserializar la respuesta.
     * @param promptInstrucciones Instrucciones de extracción específicas del módulo consumidor.
     * @param <T>                 Tipo genérico.
     * @return Instancia de T con la información extraída.
     */
    public <T> T extraerDesdeImagen(
            byte[] imagenBytes,
            String contentType,
            Class<T> tipoDestino,
            String promptInstrucciones
    ) {
        ImageFormat format = resolverImageFormat(contentType);
        ImageBlock imageBlock = ImageBlock.builder()
                .format(format)
                .source(ImageSource.builder()
                        .bytes(SdkBytes.fromByteArray(imagenBytes))
                        .build())
                .build();

        String esquemaEjemplo = construirEsquemaEjemplo(tipoDestino);
        String promptCompleto = construirPromptExtraccion(promptInstrucciones, esquemaEjemplo);

        Message userMessage = Message.builder()
                .role(ConversationRole.USER)
                .content(
                        ContentBlock.fromImage(imageBlock),
                        ContentBlock.fromText(promptCompleto)
                )
                .build();

        return ejecutarConverseYDeserializar(userMessage, tipoDestino);
    }

    /**
     * Extrae información estructurada a partir de texto plano utilizando AWS Bedrock Converse API.
     *
     * @param texto               Texto extraído previamente (ej. de un PDF digital).
     * @param tipoDestino         Clase destino donde deserializar la respuesta.
     * @param promptInstrucciones Instrucciones de extracción.
     * @param <T>                 Tipo genérico.
     * @return Instancia de T con la información extraída.
     */
    public <T> T extraerDesdeTexto(
            String texto,
            Class<T> tipoDestino,
            String promptInstrucciones
    ) {
        String esquemaEjemplo = construirEsquemaEjemplo(tipoDestino);
        String promptCompleto = construirPromptExtraccion(promptInstrucciones, esquemaEjemplo)
                + "\n\nTexto a analizar:\n" + texto;

        Message userMessage = Message.builder()
                .role(ConversationRole.USER)
                .content(ContentBlock.fromText(promptCompleto))
                .build();

        return ejecutarConverseYDeserializar(userMessage, tipoDestino);
    }

    private <T> T ejecutarConverseYDeserializar(Message userMessage, Class<T> tipoDestino) {
        String modelId = properties.getBedrock().getModelId();

        InferenceConfiguration inferenceConfig = InferenceConfiguration.builder()
                .temperature(properties.getBedrock().getTemperature().floatValue())
                .maxTokens(properties.getBedrock().getMaxTokens())
                .build();

        SystemContentBlock systemBlock = SystemContentBlock.builder()
                .text("Eres un asistente experto en extracción de datos estructurados de documentos y comprobantes financieros. "
                        + "Debes responder EXCLUSIVAMENTE con un JSON válido y bien formado que cumpla con los campos solicitados. "
                        + "No agregues introducciones, comentarios ni bloques markdown.")
                .build();

        ConverseRequest request = ConverseRequest.builder()
                .modelId(modelId)
                .system(systemBlock)
                .messages(List.of(userMessage))
                .inferenceConfig(inferenceConfig)
                .build();

        try {
            ConverseResponse response = bedrockRuntimeClient.converse(request);
            String rawOutput = response.output().message().content().getFirst().text();
            String jsonLimpio = limpiarJson(rawOutput);

            return objectMapper.readValue(jsonLimpio, tipoDestino);
        } catch (Exception e) {
            log.error("Error al procesar extracción en AWS Bedrock: {}", e.getMessage(), e);
            throw new RuntimeException("Fallo en la extracción de datos con inteligencia artificial: " + e.getMessage(), e);
        }
    }

    private ImageFormat resolverImageFormat(String contentType) {
        if (contentType == null) {
            return ImageFormat.PNG;
        }
        String mime = contentType.toLowerCase().trim();
        if (mime.contains("jpeg") || mime.contains("jpg")) {
            return ImageFormat.JPEG;
        }
        if (mime.contains("webp")) {
            return ImageFormat.WEBP;
        }
        if (mime.contains("gif")) {
            return ImageFormat.GIF;
        }
        return ImageFormat.PNG;
    }

    private String construirEsquemaEjemplo(Class<?> clazz) {
        if (clazz.isRecord()) {
            StringBuilder sb = new StringBuilder("{\n");
            RecordComponent[] components = clazz.getRecordComponents();
            for (int i = 0; i < components.length; i++) {
                RecordComponent rc = components[i];
                sb.append("  \"").append(rc.getName()).append("\": ")
                        .append(obtenerPlaceholderPorTipo(rc.getType()));
                if (i < components.length - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            sb.append("}");
            return sb.toString();
        }
        return "{}";
    }

    private String obtenerPlaceholderPorTipo(Class<?> type) {
        if (Number.class.isAssignableFrom(type) || type.isPrimitive() && (type == int.class || type == double.class || type == long.class || type == float.class)) {
            return "0.00";
        }
        if (Boolean.class.isAssignableFrom(type) || type == boolean.class) {
            return "true";
        }
        if (Map.class.isAssignableFrom(type)) {
            return "{}";
        }
        if (List.class.isAssignableFrom(type)) {
            return "[]";
        }
        return "\"valor\"";
    }

    private String construirPromptExtraccion(String promptInstrucciones, String esquemaEjemplo) {
        return (promptInstrucciones != null && !promptInstrucciones.isBlank() ? promptInstrucciones + "\n\n" : "")
                + "Instrucciones de formato:\n"
                + "1. Extrae los datos solicitados del documento.\n"
                + "2. Devuelve estrictamente un objeto JSON con este formato de propiedades (si no encuentras un dato, colócalo como null):\n"
                + esquemaEjemplo + "\n"
                + "3. Devuelve únicamente el JSON crudo, sin etiquetas de bloque ```json ni texto adicional.";
    }

    private String limpiarJson(String rawText) {
        if (rawText == null) {
            return "{}";
        }
        String clean = rawText.trim();
        if (clean.startsWith("```json")) {
            clean = clean.substring(7);
        } else if (clean.startsWith("```")) {
            clean = clean.substring(3);
        }
        if (clean.endsWith("```")) {
            clean = clean.substring(0, clean.length() - 3);
        }
        return clean.trim();
    }
}
