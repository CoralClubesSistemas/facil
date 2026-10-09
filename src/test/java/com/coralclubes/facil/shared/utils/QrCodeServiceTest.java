package com.coralclubes.facil.shared.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeServiceTest {

    private final QrCodeService qrCodeService = new QrCodeService();

    @Test
    @DisplayName("Debe generar un código QR en bytes PNG exitosamente")
    void debeGenerarQrExitosamente() {
        String contenido = "https://coralclubes.com/reserva/12345";
        byte[] qrBytes = qrCodeService.generarQr(contenido);

        assertNotNull(qrBytes);
        assertTrue(qrBytes.length > 0);
        // Verificar firma de archivo PNG (89 50 4E 47 0D 0A 1A 0A)
        assertEquals((byte) 0x89, qrBytes[0]);
        assertEquals((byte) 0x50, qrBytes[1]);
        assertEquals((byte) 0x4E, qrBytes[2]);
        assertEquals((byte) 0x47, qrBytes[3]);
    }

    @Test
    @DisplayName("Debe generar Data URI con formato PNG y Base64")
    void debeGenerarQrDataUri() {
        String contenido = "test-uuid-1234";
        String dataUri = qrCodeService.generarQrDataUri(contenido);

        assertNotNull(dataUri);
        assertTrue(dataUri.startsWith("data:image/png;base64,"));
        assertTrue(dataUri.length() > "data:image/png;base64,".length());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el contenido es nulo o vacío")
    void debeLanzarExcepcionConContenidoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> qrCodeService.generarQr(null));
        assertThrows(IllegalArgumentException.class, () -> qrCodeService.generarQr(""));
        assertThrows(IllegalArgumentException.class, () -> qrCodeService.generarQr("   "));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si las dimensiones son menores o iguales a cero")
    void debeLanzarExcepcionConDimensionesInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> qrCodeService.generarQr("valido", 0, 300));
        assertThrows(IllegalArgumentException.class, () -> qrCodeService.generarQr("valido", 300, -1));
    }
}
