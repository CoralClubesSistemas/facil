package com.coralclubes.facil.shared.infrastructure.integration.storage.event;

import java.util.Map;
import java.util.UUID;

/**
 * Evento interno de la aplicación que se publica cuando el microservicio de almacenamiento
 * procesa un archivo. Desacopla los módulos de negocio de la infraestructura de mensajería (RabbitMQ).
 */
public record StorageFileProcessedEvent(
        UUID fileId,
        String bucket,
        String path,
        String status,
        String message,
        Map<String, String> metadatos
) {
    /**
     * Obtiene un valor de los metadatos de forma segura.
     *
     * @param key Clave del metadato.
     * @return El valor del metadato, o null si no existe.
     */
    public String getMetadataValue(String key) {
        return metadatos != null ? metadatos.get(key) : null;
    }
}
