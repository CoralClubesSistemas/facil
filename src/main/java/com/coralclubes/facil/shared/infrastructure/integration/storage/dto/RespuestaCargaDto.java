package com.coralclubes.facil.shared.infrastructure.integration.storage.dto;

import java.util.UUID;

/**
 * Respuesta que entrega el Microservicio de Storage.
 */
public record RespuestaCargaDto(
        String idCorrelacion,
        UUID fileId,
        String nombreOriginal,
        String uploadUrl,
        String metodo,
        Integer expiracionSegundos
) {}
