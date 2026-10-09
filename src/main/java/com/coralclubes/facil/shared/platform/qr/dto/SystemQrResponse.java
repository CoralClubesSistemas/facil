package com.coralclubes.facil.shared.platform.qr.dto;

import com.coralclubes.facil.shared.platform.qr.enums.SystemQrStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de respuesta con la información completa de un código QR persistido.
 */
@Builder
public record SystemQrResponse(
        UUID id,
        String qrToken,
        UUID qrFileId,
        String urlDescarga,
        String module,
        String submodule,
        String actionType,
        String entityId,
        String entityType,
        SystemQrStatus status,
        Integer maxUses,
        Integer usedCount,
        Instant expiresAt,
        Instant lastUsedAt,
        String metadata,
        Instant createdAt,
        String createdBy
) {
}
