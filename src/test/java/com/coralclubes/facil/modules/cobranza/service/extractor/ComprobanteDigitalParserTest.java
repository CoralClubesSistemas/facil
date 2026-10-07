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
    @DisplayName("Debe extraer con éxito comprobante empresarial BBVA Net Cash (Mismo Banco)")
    void testParsearBbvaNetCash() {
        String textoBbvaNetCash = """
                Fecha y hora de consulta 23/09/2026 10:47:02 PM Contrato 00464392
                Nombre del Cliente PROMOTORA TURISTICA DE MANZANILLO SA DE CV
                BBVA Net Cash - Pagos Mismo Banco
                Operación autorizada
                Datos del firmante
                Usuario: ADMIN6 Poder: 100%
                Datos de la operación
                Tipo de operación: Pago Mismo Banco
                Descripción: FAC 3435 Importe de la operación: 24,648.76 MXP
                Cuenta de retiro: 0181038451 Cuenta de depósito: 0454342127
                Divisa de la cuenta: MXP Divisa de la cuenta: MXP
                Titular de la cuenta: MEXITOURS SA DE CV Titular de la cuenta: EMPRESAS FIMEX,SA DE CV
                Fecha de creación: 23/09/2026 Fecha de aplicación: 23/09/2026
                Hora: 22:47:00
                Instrumento de seguridad: ASD 6552108461 Motivo de pago: CONCILIACION CORAL VISTA D EL M
                Datos de confirmación de la transferencia
                Folio de firma: 0007979208 Folio único: I323202609232247000007979215
                Estado operación
                Porcentaje firmado: 100% Estado: Operado
                BBVA México, S.A., Institución de Banca Múltiple, Grupo Financiero BBVA México www.bbvanetcash.mx
                """;

        Optional<ComprobantePagoAnalizadoDto> resultado = parser.parsear(textoBbvaNetCash);

        assertTrue(resultado.isPresent(), "Debe poder procesar comprobante BBVA Net Cash");
        ComprobantePagoAnalizadoDto dto = resultado.get();

        assertEquals("BBVA", dto.bancoEmisor());
        assertEquals("BBVA", dto.bancoReceptor());
        assertEquals(new BigDecimal("24648.76"), dto.monto());
        assertEquals("0181038451", dto.cuentaOrdenante());
        assertEquals("0454342127", dto.cuentaBeneficiaria());
        assertEquals("23/09/2026", dto.fechaOperacion());
        assertEquals("22:47:00", dto.horaOperacion());
        assertNotNull(dto.referencia(), "Debe extraer folio único o de firma como referencia");
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
