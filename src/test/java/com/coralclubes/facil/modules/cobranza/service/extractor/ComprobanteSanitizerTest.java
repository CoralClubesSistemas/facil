package com.coralclubes.facil.modules.cobranza.service.extractor;

import com.coralclubes.facil.modules.cobranza.dto.response.ComprobantePagoAnalizadoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ComprobanteSanitizerTest {

    private final ComprobanteSanitizer sanitizer = new ComprobanteSanitizer();

    @Test
    @DisplayName("Debe deducir banco emisor y receptor a partir de sus CLABEs")
    void testDeduccionBancosPorClabe() {
        ComprobantePagoAnalizadoDto raw = new ComprobantePagoAnalizadoDto(
                "DESCONOCIDO",
                null,
                new BigDecimal("5000.00"),
                "06/10/2026",
                "12:00:00",
                "BBVA1234567890",
                "12345",
                "012180001234567890", // 012 = BBVA
                "014180009876543210", // 014 = SANTANDER
                "CORAL CLUBES SA DE CV",
                "JUAN PEREZ",
                "Pago membresia",
                "TRANSFERENCIA"
        );

        ComprobantePagoAnalizadoDto res = sanitizer.sanitizar(raw);

        assertEquals("BBVA", res.bancoEmisor());
        assertEquals("SANTANDER", res.bancoReceptor());
        assertEquals("2026-10-06", res.fechaOperacion());
        assertEquals("SPEI", res.tipoOperacion());
    }

    @Test
    @DisplayName("Debe descartar referencias que provienen de pies de página o leyendas de CONDUSEF")
    void testDescarteReferenciaPieDePagina() {
        ComprobantePagoAnalizadoDto raw = new ComprobantePagoAnalizadoDto(
                "BBVA",
                "SANTANDER",
                new BigDecimal("1000.00"),
                "2026-10-06",
                null,
                null,
                "CONDUSEF 55 53 400 999 HOJA 1 DE 1", // Pie de página sucio
                null,
                null,
                "CORAL CLUBES",
                "CLIENTE EJEMPLO",
                "Cuota",
                "TRANSFERENCIA"
        );

        ComprobantePagoAnalizadoDto res = sanitizer.sanitizar(raw);

        assertNull(res.referencia(), "La referencia debió ser descartada por contener CONDUSEF/HOJA");
    }

    @Test
    @DisplayName("Debe corregir roles cuando Coral Clubes fue asignado erróneamente como ordenante")
    void testInversionRolesCoralClubes() {
        ComprobantePagoAnalizadoDto raw = new ComprobantePagoAnalizadoDto(
                "BBVA",
                "BANORTE",
                new BigDecimal("2500.00"),
                "2026-10-06",
                null,
                "RASTREO12345678",
                "778899",
                "072180000000000001",
                "012180000000000002",
                "JUAN SOCIO",         // Beneficiario erróneo
                "CORAL CLUBES",       // Ordenante erróneo (debe invertirse)
                "Pago cuota",
                "SPEI"
        );

        ComprobantePagoAnalizadoDto res = sanitizer.sanitizar(raw);

        assertEquals("CORAL CLUBES", res.beneficiario(), "Coral Clubes debe ser el beneficiario");
        assertEquals("JUAN SOCIO", res.ordenante(), "El cliente debe ser el ordenante");
    }
}
