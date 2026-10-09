package com.coralclubes.facil.shared.platform.qr.enums;

/**
 * Estados del ciclo de vida de un código QR en el sistema.
 * Mapea los valores permitidos en la columna status de la tabla system_qrs.
 */
public enum SystemQrStatus {
    ACTIVE,
    INACTIVE,
    REVOKED,
    USED,
    EXPIRED
}
