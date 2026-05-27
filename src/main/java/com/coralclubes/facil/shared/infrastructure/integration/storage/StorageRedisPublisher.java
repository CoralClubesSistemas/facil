package com.coralclubes.facil.shared.infrastructure.integration.storage;

import com.coralclubes.facil.shared.events.dto.StorageFileProcessedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

/**
 * Escucha los eventos internos de archivos procesados y los publica en Redis
 * para que el API Gateway los distribuya al frontend vía WebSockets.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StorageRedisPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @EventListener
    public void manejarEventoInterno(StorageFileProcessedEvent event) {
        /* generalmente y dentro del sistema FACIL todos los archivos cargados al microservicio de storage
        incluiran este campo en la metadata "subidoPor"
        aun que podemos agregar validaciones para evitar publicar eventos sin esta información */
        String username = obtenerPrimerMetadatoDisponible(event, "subidoPor", "username", "uploader", "user", "usuario");
        String system = event.system();

        if (username == null || system == null) {
            log.warn("No se pudo reenviar el evento a Redis para el archivo {}: Faltan metadatos de enrutamiento (username/system)", event.fileId());
            return;
        }

        // Construimos el mismo patrón de canal que el Gateway escucha
        String channel = "user-events:" + system.toLowerCase() + ":" + username.toLowerCase();

        try {
            // Convertimos el evento a un JSON String uniforme
            String jsonPayload = objectMapper.writeValueAsString(event);

            // Publicamos en el bus de Redis
            redisTemplate.convertAndSend(channel, jsonPayload);
            log.info("Evento de almacenamiento publicado en Redis en el canal '{}' para el archivo {}", channel, event.fileId());

        } catch (Exception e) {
            log.error("Error al serializar y publicar evento en Redis para el archivo " + event.fileId(), e);
        }
    }

    // HELPER: devuelve el primer campo de la metadata que coincida con las claves proporcionadas y que tenga un valor no nulo ni vacío.
    private String obtenerPrimerMetadatoDisponible(StorageFileProcessedEvent event, String... claves) {
        return Stream.of(claves).map(event::getMetadataValue).filter(valor -> valor != null && !valor.isBlank()).findFirst().orElse(null);
    }
}