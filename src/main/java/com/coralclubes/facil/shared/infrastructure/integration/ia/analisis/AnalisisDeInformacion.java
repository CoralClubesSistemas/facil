package com.coralclubes.facil.shared.infrastructure.integration.ia.analisis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalisisDeInformacion {

    private final ObjectMapper objectMapper;

    private final String MODEL_ID = "amazon.nova-micro-v1:0";

    private final BedrockRuntimeClient bedrockClient = BedrockRuntimeClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();

    public String analizarData(String systemPrompt, String jsonData) {
        try {
            // 1. Armar el prompt del usuario
            String promptUsuario = "Analiza esta información y devuelve ÚNICAMENTE un objeto JSON válido, sin texto adicional:\n" + jsonData;

            // 2. Construir la estructura exacta que requiere Amazon Nova usando Map
            // Esto asegura que Jackson escape correctamente todos los caracteres del JSON de tu DB
            Map<String, Object> payloadMap = Map.of(
                    "system", List.of(
                            Map.of("text", systemPrompt)
                    ),
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", List.of(
                                            Map.of("text", promptUsuario)
                                    )
                            )
                    ),
                    "inferenceConfig", Map.of(
                            "max_new_tokens", 1000,
                            "temperature", 0.5
                    )
            );

            // 3. Convertir el Map a String JSON de forma segura
            String payload = objectMapper.writeValueAsString(payloadMap);

            // 4. Invocar a Bedrock
            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(MODEL_ID)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(payload))
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);

            // 5. Extraer la respuesta (El path que pusiste para Nova es correcto)
            JsonNode responseNode = objectMapper.readTree(response.body().asUtf8String());

            return responseNode
                    .get("output")
                    .get("message")
                    .get("content")
                    .get(0)
                    .get("text")
                    .asText();

        } catch (Exception e) {
            throw new RuntimeException("Error al comunicarse con Amazon Bedrock: " + e.getMessage(), e);
        }
    }
}