package com.coralclubes.facil.modules.cobranza.service.extractor;

import com.coralclubes.facil.modules.cobranza.dto.response.ComprobantePagoAnalizadoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ComprobanteDigitalParserTest {

    private final ComprobanteDigitalParser parser = new ComprobanteDigitalParser();

    @Test
    @DisplayName("Debe extraer con éxito los datos bancarios de un comprobante digital SPEI")
    void testParsearExitoso() {
        String textoBancario = """
                BBVA MEXICO
                COMPROBANTE DE TRANSFERENCIA INTERBANCARIA (SPEI)
                Fecha de operación: 06/10/2026
                Hora: 14:35:20
                Clave de rastreo: BBVA061026987654321
                Referencia: 1234567
                Cuenta ordenante: 012180001234567890
                Cuenta beneficiaria: 014180009876543210
                Beneficiario: CORAL CLUBES SA DE CV
                Importe: $15,450.00 MXN
                Concepto: Cuota Mantenimiento
                """;

        Optional<ComprobantePagoAnalizadoDto> resultado = parser.parsear(textoBancario);

        assertTrue(resultado.isPresent(), "El resultado debería estar presente");
        ComprobantePagoAnalizadoDto dto = resultado.get();

        assertEquals("BBVA", dto.bancoEmisor());
        assertEquals(new BigDecimal("15450.00"), dto.monto());
        assertEquals("BBVA061026987654321", dto.claveRastreo());
        assertEquals("1234567", dto.referencia());
        assertEquals("06/10/2026", dto.fechaOperacion());
        assertEquals("14:35:20", dto.horaOperacion());
        assertEquals("014180009876543210", dto.cuentaBeneficiaria());
    }

    @Test
    @DisplayName("Debe retornar Optional.empty() si el texto no contiene monto para forzar fallback a IA")
    void testParsearSinMonto() {
        String textoIncompleto = """
                BBVA BANCOMER
                Clave de rastreo: 1234567890ABCDEF
                Fecha: 05/10/2026
                """;

        Optional<ComprobantePagoAnalizadoDto> resultado = parser.parsear(textoIncompleto);

        assertTrue(resultado.isEmpty(), "Debe retornar empty cuando no hay monto");
    }

    @Test
    @DisplayName("Debe retornar Optional.empty() si el texto es nulo o vacío")
    void testParsearTextoVacio() {
        assertTrue(parser.parsear(null).isEmpty());
        assertTrue(parser.parsear("   ").isEmpty());
    }
}
