package com.coralclubes.facil.shared.utils;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Utilidad para la generación de identificadores únicos universales (UUID).
 * Implementa la generación de UUID versión 7 (time-ordered) conforme al RFC 9562.
 */
public final class UuidUtils {

    private static final SecureRandom RANDOM = new SecureRandom();

    private UuidUtils() {
    }

    /**
     * Genera un UUID versión 7 (time-ordered) conforme a RFC 9562.
     * Los primeros 48 bits corresponden al timestamp Unix en milisegundos,
     * garantizando ordenamiento cronológico e indexación B-Tree de alto rendimiento en base de datos.
     *
     * @return Instancia de {@link UUID} versión 7.
     */
    public static UUID generateV7() {
        long timestamp = System.currentTimeMillis();
        long randomA = RANDOM.nextLong() & 0x0FFFL;

        // 48 bits de timestamp ms + 4 bits de versión 7 (0x7000) + 12 bits de entropía
        long msb = (timestamp << 16) | 0x7000L | randomA;

        // 2 bits de variante RFC (0b10 -> 0x8) + 62 bits de entropía
        long lsb = (0x8000_0000_0000_0000L) | (RANDOM.nextLong() & 0x3FFF_FFFF_FFFF_FFFFL);

        return new UUID(msb, lsb);
    }
}
