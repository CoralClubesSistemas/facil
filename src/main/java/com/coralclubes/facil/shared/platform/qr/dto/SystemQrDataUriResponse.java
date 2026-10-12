package com.coralclubes.facil.shared.platform.qr.dto;

import lombok.Builder;

import java.util.UUID;

/**
 * DTO que contiene la información del código QR persistido y su representación como Data URI Base64.
 *
 * @param qr      Respuesta con los metadatos del QR persistido.
 * @param dataUri Representación en Data URI Base64 ("data:image/png;base64,...") para renderizado inmediato.
 */
@Builder
public record SystemQrDataUriResponse(
        SystemQrResponse qr,
        String dataUri
) {
    public UUID qrFileId() {
        return qr != null ? qr.qrFileId() : null;
    }
}
