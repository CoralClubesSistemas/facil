package com.coralclubes.facil.shared.platform.qr.dto;

import com.coralclubes.facil.shared.platform.qr.enums.SystemQrStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.Instant;

/**
 * DTO para la solicitud de creación de un código QR en el sistema.
 * El token es generado internamente de forma criptográficamente segura por el servicio.
 */
@Builder
public record CreateQrRequest(
        @NotBlank(message = "El módulo es obligatorio")
        @Size(max = 50, message = "El nombre del módulo no puede exceder 50 caracteres")
        String module,

        @Size(max = 50, message = "El sub-módulo no puede exceder 50 caracteres")
        String submodule,

        @NotBlank(message = "El tipo de acción es obligatorio")
        @Size(max = 50, message = "El tipo de acción no puede exceder 50 caracteres")
        String actionType,

        @NotBlank(message = "El ID de la entidad es obligatorio")
        @Size(max = 64, message = "El ID de la entidad no puede exceder 64 caracteres")
        String entityId,

        @NotBlank(message = "El tipo de entidad es obligatorio")
        @Size(max = 50, message = "El tipo de entidad no puede exceder 50 caracteres")
        String entityType,

        /**
         * Ruta lógica o URL base de almacenamiento en el servicio de archivos (ej. 'reservaciones/qrs').
         */
        @Size(max = 255, message = "La URL de almacenamiento no puede exceder 255 caracteres")
        String urlAlmacenamiento,

        SystemQrStatus status,

        Integer maxUses,

        Instant expiresAt,

        String metadata,

        @Size(max = 100, message = "El creador no puede exceder 100 caracteres")
        String createdBy
) {
}