package com.coralclubes.facil.shared.platform.qr.dto;

import com.coralclubes.facil.shared.platform.qr.enums.SystemQrStatus;
import lombok.Builder;

import java.util.UUID;

/**
 * DTO de respuesta para la resolución de un código QR.
 * Contiene únicamente los datos no sensibles necesarios para la identificación de la acción
 * y la ruta de navegación del módulo destino en el sistema.
 *
 * @param id          Identificador único del QR.
 * @param token       Token alfanumérico público del QR.
 * @param module      Módulo emisor (ej. RESERVACIONES).
 * @param submodule   Submódulo emisor (ej. RESERVACIONES_RECEPCION).
 * @param actionType  Tipo de acción a ejecutar (ej. CHECK_IN).
 * @param entityId    Identificador de la entidad vinculada (ej. folios de reservación).
 * @param entityType  Tipo de entidad vinculada (ej. RESERVATION).
 * @param status      Estado actual del QR (ej. ACTIVE).
 * @param metadata    Contexto adicional o metadata del QR.
 * @param rutaModulo  Ruta de navegación web del módulo/submódulo resuelto.
 */
@Builder
public record SystemQrResolucionResponse(
        UUID id,
        String token,
        String module,
        String submodule,
        String actionType,
        String entityId,
        String entityType,
        SystemQrStatus status,
        String metadata,
        String rutaModulo
) {
}
