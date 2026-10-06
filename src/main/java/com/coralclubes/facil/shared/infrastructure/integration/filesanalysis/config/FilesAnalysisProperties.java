package com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.clients.files-analysis")
public class FilesAnalysisProperties {

    /**
     * Proveedor del servicio: 'local' (Fase 1 - embebido) o 'remote' (Fase 2 - microservicio).
     */
    private String provider = "local";

    /**
     * URL del microservicio files-analysis (para Fase 2).
     */
    private String url;

    /**
     * API Key para comunicación con microservicio (Fase 2).
     */
    private String apiKey;

    /**
     * Configuración de AWS Bedrock.
     */
    private BedrockProperties bedrock = new BedrockProperties();

    @Getter
    @Setter
    public static class BedrockProperties {
        private String region = "us-east-1";
        private String modelId = "us.amazon.nova-pro-v1:0";
        private Double temperature = 0.0;
        private Integer maxTokens = 2048;
        private String accessKey;
        private String secretKey;
    }
}
