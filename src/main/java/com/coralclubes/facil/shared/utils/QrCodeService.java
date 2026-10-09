package com.coralclubes.facil.shared.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

/**
 * Servicio utilitario para la generacion de codigos QR.
 * Utilizamos la libreria zxing para generar los codigos.
 */
@Service
public class QrCodeService {

    /**
     * Recibe un string y devuelve un arreglo de bytes con la imagen del QR en formato PNG.
     * Tamaño por defecto: 300x300 px.
     *
     * @param contenido El contenido del QR
     * @return Un arreglo de bytes con la imagen del QR en formato PNG
     * @throws IllegalArgumentException si el contenido es nulo o vacío
     * @throws IllegalStateException    si no se puede generar el código QR
     */
    public byte[] generarQr(String contenido) {
        return generarQr(contenido, 300, 300);
    }

    /**
     * Recibe un string y devuelve un arreglo de bytes con la imagen del QR en formato PNG.
     *
     * @param contenido El contenido del QR
     * @param ancho     El ancho del QR
     * @param alto      El alto del QR
     * @return Un arreglo de bytes con la imagen del QR en formato PNG
     * @throws IllegalArgumentException si el contenido es nulo o vacío, o si las dimensiones son menores o iguales a cero
     * @throws IllegalStateException    si no se puede generar el código QR
     */
    public byte[] generarQr(
            String contenido,
            int ancho,
            int alto
    ) {
        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException(
                    "El contenido del QR no puede estar vacío"
            );
        }

        if (ancho <= 0 || alto <= 0) {
            throw new IllegalArgumentException(
                    "Las dimensiones deben ser mayores que cero"
            );
        }

        try {
            var hints = Map.of(
                    EncodeHintType.CHARACTER_SET, "UTF-8",
                    EncodeHintType.ERROR_CORRECTION,
                    ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 4
            );

            var writer = new QRCodeWriter();

            var matriz = writer.encode(
                    contenido,
                    BarcodeFormat.QR_CODE,
                    ancho,
                    alto,
                    hints
            );

            try (var output = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(
                        matriz,
                        "PNG",
                        output
                );

                return output.toByteArray();
            }

        } catch (WriterException | IOException e) {
            throw new IllegalStateException(
                    "No fue posible generar el código QR", e
            );
        }
    }

    /**
     * Recibe un string y devuelve un Data URI con la imagen del QR en formato PNG.
     * Tamaño por defecto: 300x300 px.
     *
     * @param contenido El contenido del QR
     * @return Un Data URI con la imagen del QR en formato PNG (archivo en base64)
     * @throws IllegalArgumentException si el contenido es nulo o vacío
     * @throws IllegalStateException    si no se puede generar el código QR
     */
    public String generarQrDataUri(String contenido) {
        return generarQrDataUri(contenido, 300, 300);
    }

    /**
     * Recibe un string y devuelve un Data URI con la imagen del QR en formato PNG.
     *
     * @param contenido El contenido del QR
     * @param ancho     El ancho del QR
     * @param alto      El alto del QR
     * @return Un Data URI con la imagen del QR en formato PNG (archivo en base64)
     * @throws IllegalArgumentException si el contenido es nulo o vacío, o si las dimensiones son menores o iguales a cero
     * @throws IllegalStateException    si no se puede generar el código QR
     */
    public String generarQrDataUri(String contenido, int ancho, int alto) {
        byte[] qrBytes = generarQr(contenido, ancho, alto);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(qrBytes);
    }
}
