package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

import java.util.UUID;

/**
 * Record que representa el resultado de la generación del código QR oficial para una reservación,
 * conteniendo tanto el identificador del archivo en Storage como su representación Data URI para incrustar en el documento.
 *
 * @param qrFileId       Identificador del archivo del código QR en Storage.
 * @param qrCodeDataUri  Representación Data URI ("data:image/png;base64,...") de la imagen del QR.
 */
@Builder
public record ResultadoQrReservacion(
        UUID qrFileId,
        String qrCodeDataUri
) {
}
