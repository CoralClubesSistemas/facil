package com.coralclubes.facil.modules.cobranza.engines;

import com.coralclubes.facil.modules.cobranza.dto.projection.MovimientoIntencionPersistenciaDto;
import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.SimularCalculoDescuentoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.SimularCalculoDescuentoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CobranzaCalculoEngineTest {

    private CobranzaCalculoEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CobranzaCalculoEngine();
    }

    @Test
    @DisplayName("Debe calcular correctamente el porcentaje acumulado en cascada: 10% y 5% -> 14.50%")
    void testCalcularPorcentajeEfectivoCascada() {
        List<BigDecimal> porcentajes = List.of(new BigDecimal("10.00"), new BigDecimal("5.00"));
        BigDecimal resultado = engine.calcularPorcentajeEfectivoCascada(porcentajes);

        assertEquals(new BigDecimal("14.50"), resultado);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el porcentaje en cascada excede el autorizado y usuarioAutoriza es null")
    void testValidarTopeAutorizadoExcedidoSinUsuarioAutoriza() {
        List<BigDecimal> porcentajes = List.of(new BigDecimal("20.00"), new BigDecimal("10.00")); // ~28%
        BigDecimal limiteAutorizado = new BigDecimal("25.00");

        SimularCalculoDescuentoRequest request = SimularCalculoDescuentoRequest.builder()
                .porcentajesDescuentoCascada(porcentajes)
                .usuarioAutoriza(null)
                .movimientos(List.of(
                        new GenerarOrdenCobranzaMovimientoRequest(
                                1, new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                                BigDecimal.ZERO, BigDecimal.ZERO, null, null
                        )
                ))
                .build();

        assertThrows(IllegalArgumentException.class, () -> engine.simularCalculo(request, limiteAutorizado));
    }

    @Test
    @DisplayName("Debe permitir el cálculo si el porcentaje excede el autorizado cuando usuarioAutoriza está presente")
    void testPermitirCalculoCuandoUsuarioAutorizaPresente() {
        List<BigDecimal> porcentajes = List.of(new BigDecimal("20.00"), new BigDecimal("10.00")); // ~28%
        BigDecimal limiteAutorizado = new BigDecimal("25.00");

        SimularCalculoDescuentoRequest request = SimularCalculoDescuentoRequest.builder()
                .porcentajesDescuentoCascada(porcentajes)
                .usuarioAutoriza("SUPERVISOR_01")
                .movimientos(List.of(
                        new GenerarOrdenCobranzaMovimientoRequest(
                                1, new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                                BigDecimal.ZERO, BigDecimal.ZERO, null, null
                        )
                ))
                .build();

        SimularCalculoDescuentoResponse response = assertDoesNotThrow(() -> engine.simularCalculo(request, limiteAutorizado));
        assertNotNull(response);
        assertEquals(new BigDecimal("28.00"), response.porcentajeRealAplicable());
        assertTrue(response.requiereAutorizacion());
        assertTrue(response.autorizado());
        assertEquals(new BigDecimal("280.00"), response.montoTotalDescuento());
    }

    @Test
    @DisplayName("Debe simular cálculo con IVA agregado correctamente")
    void testSimularCalculoIvaAgregado() {
        SimularCalculoDescuentoRequest request = SimularCalculoDescuentoRequest.builder()
                .porcentajesDescuentoCascada(List.of(new BigDecimal("10.00")))
                .agregarIva(true)
                .ivaIncluido(false)
                .movimientos(List.of(
                        new GenerarOrdenCobranzaMovimientoRequest(
                                101, new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                                BigDecimal.ZERO, BigDecimal.ZERO, null, null
                        )
                ))
                .build();

        SimularCalculoDescuentoResponse response = engine.simularCalculo(request, new BigDecimal("15.00"));

        assertNotNull(response);
        assertEquals(new BigDecimal("10.00"), response.porcentajeRealAplicable());
        assertEquals(new BigDecimal("1000.00"), response.montoTotalOriginal());
        assertEquals(new BigDecimal("100.00"), response.montoTotalDescuento());
        assertEquals(new BigDecimal("900.00"), response.montoTotalConDescuento());
        assertEquals(new BigDecimal("144.00"), response.montoTotalIva()); // 900 * 0.16 = 144
        assertEquals(new BigDecimal("1044.00"), response.montoTotalFinal());
        assertEquals(1, response.items().size());
        assertEquals(new BigDecimal("100.00"), response.items().getFirst().montoDescuento());
    }

    @Test
    @DisplayName("Debe calcular valores netos y bases imponibles para persistencia con IVA incluido")
    void testProcesarParaPersistenciaIvaIncluido() {
        GenerarOrdenCobranzaRequest request = GenerarOrdenCobranzaRequest.builder()
                .membresia("MEM-123")
                .agregarIva(true)
                .ivaIncluido(true)
                .porcentajesDescuentoCascada(List.of(new BigDecimal("10.00")))
                .movimientos(List.of(
                        new GenerarOrdenCobranzaMovimientoRequest(
                                101, new BigDecimal("1160.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                                BigDecimal.ZERO, BigDecimal.ZERO, null, null
                        )
                ))
                .build();

        List<MovimientoIntencionPersistenciaDto> resultado = engine.procesarParaPersistencia(request, new BigDecimal("20.00"));

        assertEquals(1, resultado.size());
        MovimientoIntencionPersistenciaDto dto = resultado.getFirst();

        // 1160 con 10% desc = 116 desc -> 1044 neto con IVA
        // Base gravable de 1044 / 1.16 = 900.00 -> IVA = 144.00
        assertEquals(new BigDecimal("144.00"), dto.montoIva());
        assertEquals(new BigDecimal("1000.00"), dto.montoCapital()); // 1160 / 1.16 = 1000
        assertEquals(new BigDecimal("100.00"), dto.totalDescuento()); // 116 / 1.16 = 100
    }
}
