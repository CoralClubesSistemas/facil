package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

import java.util.UUID;

/**
 * Record que contiene los identificadores resultantes de la generación de la carta de ocupación
 * y su respectivo código QR oficial almacenado.
 *
 * @param uuidPdf  Identificador del archivo PDF de la carta de ocupación en Storage.
 * @param qrFileId Identificador del archivo de imagen del código QR en Storage.
 */
@Builder
public record ResultadoCartaOcupacion(
        UUID uuidPdf,
        UUID qrFileId
) {
}
