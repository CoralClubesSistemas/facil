package com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

@Configuration
@RequiredArgsConstructor
public class BedrockConfig {

    private final FilesAnalysisProperties properties;

    @Bean
    @ConditionalOnProperty(name = "app.clients.files-analysis.provider", havingValue = "local", matchIfMissing = true)
    public BedrockRuntimeClient bedrockRuntimeClient() {
        String regionStr = properties.getBedrock().getRegion();
        Region region = (regionStr != null && !regionStr.isBlank())
                ? Region.of(regionStr)
                : Region.US_EAST_1;

        return BedrockRuntimeClient.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
