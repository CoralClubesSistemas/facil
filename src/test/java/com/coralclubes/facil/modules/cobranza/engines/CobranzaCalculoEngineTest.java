package com.coralclubes.facil.modules.cobranza.engines;

import com.coralclubes.facil.modules.cobranza.dto.projection.MovimientoIntencionPersistenciaDto;
import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.SimularCalculoDescuentoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.ItemCalculoDescuentoDto;
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

    private GenerarOrdenCobranzaMovimientoRequest crearMovimiento(
            int id, BigDecimal capital, BigDecimal descuento, String usuarioAutoriza, List<BigDecimal> cascada
    ) {
        return GenerarOrdenCobranzaMovimientoRequest.builder()
                .idMovimiento(id)
                .montoCapital(capital)
                .montoInteres(BigDecimal.ZERO)
                .interesPago(BigDecimal.ZERO)
                .interesesBonificados(BigDecimal.ZERO)
                .totalDescuento(descuento != null ? descuento : BigDecimal.ZERO)
                .usuarioAutoriza(usuarioAutoriza)
                .porcentajesDescuentoCascada(cascada)
                .build();
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
                .movimientos(List.of(crearMovimiento(1, new BigDecimal("1000.00"), BigDecimal.ZERO, null, null)))
                .build();

        assertThrows(com.coralclubes.facil.shared.infrastructure.exceptions.custom.PercentageExceeded.class,
                () -> engine.simularCalculo(request, limiteAutorizado));
    }

    @Test
    @DisplayName("Debe permitir el cálculo si el porcentaje excede el autorizado cuando usuarioAutoriza está presente")
    void testPermitirCalculoCuandoUsuarioAutorizaPresente() {
        List<BigDecimal> porcentajes = List.of(new BigDecimal("20.00"), new BigDecimal("10.00")); // ~28%
        BigDecimal limiteAutorizado = new BigDecimal("25.00");

        SimularCalculoDescuentoRequest request = SimularCalculoDescuentoRequest.builder()
                .porcentajesDescuentoCascada(porcentajes)
                .usuarioAutoriza("SUPERVISOR_01")
                .movimientos(List.of(crearMovimiento(1, new BigDecimal("1000.00"), BigDecimal.ZERO, null, null)))
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
                .movimientos(List.of(crearMovimiento(101, new BigDecimal("1000.00"), BigDecimal.ZERO, null, null)))
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
                .movimientos(List.of(crearMovimiento(101, new BigDecimal("1160.00"), BigDecimal.ZERO, null, null)))
                .build();

        List<MovimientoIntencionPersistenciaDto> resultado = engine.procesarParaPersistencia(request, new BigDecimal("20.00"));

        assertEquals(1, resultado.size());
        MovimientoIntencionPersistenciaDto dto = resultado.getFirst();

        assertEquals(new BigDecimal("144.00"), dto.montoIva());
        assertEquals(new BigDecimal("1000.00"), dto.montoCapital());
        assertEquals(new BigDecimal("100.00"), dto.totalDescuento());
    }

    @Test
    @DisplayName("Debe aplicar cascada individual por movimiento y respetar cascada global en los movimientos sin cascada individual")
    void testCascadaIndividualYGlobalHibrida() {
        GenerarOrdenCobranzaMovimientoRequest mov1 = crearMovimiento(
                1, new BigDecimal("1000.00"), BigDecimal.ZERO, "SUPERVISOR_INDIVIDUAL",
                List.of(new BigDecimal("20.00"), new BigDecimal("10.00")) // 28% individual
        );

        GenerarOrdenCobranzaMovimientoRequest mov2 = crearMovimiento(
                2, new BigDecimal("1000.00"), BigDecimal.ZERO, null, null // Hereda global 10%
        );

        SimularCalculoDescuentoRequest request = SimularCalculoDescuentoRequest.builder()
                .porcentajesDescuentoCascada(List.of(new BigDecimal("10.00"))) // Cascada global: 10%
                .movimientos(List.of(mov1, mov2))
                .build();

        SimularCalculoDescuentoResponse response = engine.simularCalculo(request, new BigDecimal("15.00"));

        assertNotNull(response);
        assertEquals(2, response.items().size());

        // Movimiento 1 (Cascada individual 28%):
        ItemCalculoDescuentoDto item1 = response.items().get(0);
        assertEquals(new BigDecimal("28.00"), item1.porcentajeAplicado());
        assertEquals(new BigDecimal("280.00"), item1.montoDescuento());
        assertEquals(new BigDecimal("720.00"), item1.montoCapitalConDescuento());
        assertTrue(item1.requiereAutorizacion()); // 28% > 15%
        assertTrue(item1.autorizado()); // Tiene SUPERVISOR_INDIVIDUAL

        // Movimiento 2 (Hereda cascada global 10%):
        ItemCalculoDescuentoDto item2 = response.items().get(1);
        assertEquals(new BigDecimal("10.00"), item2.porcentajeAplicado());
        assertEquals(new BigDecimal("100.00"), item2.montoDescuento());
        assertEquals(new BigDecimal("900.00"), item2.montoCapitalConDescuento());
        assertFalse(item2.requiereAutorizacion()); // 10% <= 15%

        // Total general de descuentos: 280 + 100 = 380 (19.00% ponderado sobre $2000)
        assertEquals(new BigDecimal("380.00"), response.montoTotalDescuento());
        assertEquals(new BigDecimal("19.00"), response.porcentajeRealAplicable());
    }

    @Test
    @DisplayName("Debe lanzar excepción si un movimiento individual con cascada excede el límite y no tiene usuario autorizador")
    void testCascadaIndividualExcedeLimiteSinAutorizacion() {
        GenerarOrdenCobranzaMovimientoRequest mov = crearMovimiento(
                1, new BigDecimal("1000.00"), BigDecimal.ZERO, null,
                List.of(new BigDecimal("20.00"), new BigDecimal("10.00")) // 28%
        );

        SimularCalculoDescuentoRequest request = SimularCalculoDescuentoRequest.builder()
                .movimientos(List.of(mov))
                .build();

        assertThrows(com.coralclubes.facil.shared.infrastructure.exceptions.custom.PercentageExceeded.class,
                () -> engine.simularCalculo(request, new BigDecimal("15.00")));
    }
}
