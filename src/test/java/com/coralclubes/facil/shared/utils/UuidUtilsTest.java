package com.coralclubes.facil.shared.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UuidUtilsTest {

    @Test
    @DisplayName("Debe generar UUID con versión 7 y variante 2 conforme a RFC 9562")
    void debeGenerarUuidV7Valido() {
        UUID uuid = UuidUtils.generateV7();

        assertNotNull(uuid);
        assertEquals(7, uuid.version(), "La versión del UUID debe ser 7");
        assertEquals(2, uuid.variant(), "La variante del UUID debe ser 2 (RFC 4122/9562)");
    }

    @Test
    @DisplayName("Debe generar UUIDs secuenciales cronológicamente ordenados")
    void debeGenerarUuidsOrdenados() throws InterruptedException {
        UUID first = UuidUtils.generateV7();
        Thread.sleep(2); // Asegurar avance de milisegundo
        UUID second = UuidUtils.generateV7();

        assertNotEquals(first, second);
        assertTrue(first.compareTo(second) < 0, "El primer UUID v7 debe ser menor que el segundo debido al orden cronológico");
    }
}
