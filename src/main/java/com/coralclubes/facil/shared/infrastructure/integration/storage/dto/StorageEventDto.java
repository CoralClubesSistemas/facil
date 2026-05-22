package com.coralclubes.facil.shared.infrastructure.integration.storage.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 * Evento de almacenamiento que viaja por RabbitMQ desde el microservicio Coral Almacenamiento.
 */
@Builder
public record StorageEventDto(
        UUID fileId,
        String bucket,
        String path,
        String status,
        String system,
        String message,
        Map<String, String> metadatos
) implements Serializable {}
