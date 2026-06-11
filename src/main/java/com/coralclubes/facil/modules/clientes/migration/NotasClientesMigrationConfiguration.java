package com.coralclubes.facil.modules.clientes.migration;

import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.logging.BusinessLogger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuración dedicada para el proceso de migración de notas de clientes.
 * Proporciona un RestClient y un StorageClient especializados con tiempos de espera prolongados (5 minutos)
 * para evitar excepciones de Timeout al procesar archivos adjuntos pesados en base 64.
 */
@Configuration
public class NotasClientesMigrationConfiguration {

    @Bean
    public RestClient migrationRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setReadTimeout(300000);   // 5 minutos en milisegundos
        requestFactory.setConnectTimeout(30000);  // 30 segundos en milisegundos

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public StorageClient migrationStorageClient(
            BusinessLogger logger,
            @Qualifier("migrationRestClient") RestClient migrationRestClient) {
        return new StorageClient(logger, migrationRestClient);
    }
}
