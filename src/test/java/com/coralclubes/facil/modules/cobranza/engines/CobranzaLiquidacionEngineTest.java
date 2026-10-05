package com.coralclubes.facil.modules.cobranza.engines;

import com.coralclubes.facil.modules.cobranza.dto.projection.ContextoFinalizacionOrdenResponse;
import com.coralclubes.facil.modules.cobranza.dto.projection.ContextoIntencionOrdenDto;
import com.coralclubes.facil.modules.cobranza.dto.request.AplicarCierreOrdenPayloadDto;
import com.coralclubes.facil.modules.cobranza.dto.request.PadreActualizarLiquidacionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CobranzaLiquidacionEngineTest {

    private CobranzaLiquidacionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CobranzaLiquidacionEngine();
    }

    @Test
    @DisplayName("IVA Agregado: El abono al padre solo debe considerar el capital y NO descontar el IVA del saldo pendiente")
    void testArmarPlanDeLiquidacionIvaAgregado() {
        // Dado un movimiento original con saldo pendiente de $1000
        // Se cobra $1000 de capital + $160 de IVA agregado
        ContextoIntencionOrdenDto intencion = ContextoIntencionOrdenDto.builder()
                .idMovimiento(101)
                .montoCapital(new BigDecimal("1000.00"))
                .montoIva(new BigDecimal("160.00"))
                .ivaIncluido(false)
                .interesTotalCargo(BigDecimal.ZERO)
                .interesPago(BigDecimal.ZERO)
                .montoIvaInteres(BigDecimal.ZERO)
                .interesBonificado(BigDecimal.ZERO)
                .totalDescuento(BigDecimal.ZERO)
                .saldoPendienteActual(new BigDecimal("1000.00"))
                .build();

        ContextoFinalizacionOrdenResponse contexto = ContextoFinalizacionOrdenResponse.builder()
                .ordenUuid("a33f8c9c-995f-f111-b368-f4b52065acb7")
                .serieReciboId(912)
                .serieReciboDescripcion("AUTOMATICA")
                .totalPagado(new BigDecimal("1160.00"))
                .lugarPagoId(647)
                .intenciones(List.of(intencion))
                .build();

        AplicarCierreOrdenPayloadDto payload = engine.armarPlanDeLiquidacion(contexto, 912, "LVIVAR");

        assertNotNull(payload);
        assertEquals(new BigDecimal("1160.00"), payload.importeRecibo());

        // Verificamos la amortización del padre:
        // Solo debe amortizar los $1000 de capital
        assertEquals(1, payload.padresActualizar().size());
        PadreActualizarLiquidacionDto padre = payload.padresActualizar().getFirst();
        assertEquals(101, padre.idMovimiento());
        assertEquals(new BigDecimal("1000.00"), padre.montoAbonar());
        assertEquals(new BigDecimal("0.00"), padre.nuevoSaldoPendiente());
        assertEquals(603, padre.nuevoEstatusId()); // Estatus Pagado

        // Verificamos que se crearon los movimientos de CARGO_IVA y PAGO_IVA
        assertTrue(payload.movimientosNuevos().stream().anyMatch(m -> "CARGO_IVA".equals(m.tipoRegistro())));
        assertTrue(payload.movimientosNuevos().stream().anyMatch(m -> "PAGO_IVA".equals(m.tipoRegistro())));
    }

    @Test
    @DisplayName("IVA Incluido: El abono al padre debe considerar base + IVA para liquidar completamente el saldo")
    void testArmarPlanDeLiquidacionIvaIncluido() {
        // Dado un movimiento con saldo pendiente de $1160 (que ya traía IVA incluido)
        // La base es $1000 y el IVA es $160
        ContextoIntencionOrdenDto intencion = ContextoIntencionOrdenDto.builder()
                .idMovimiento(202)
                .montoCapital(new BigDecimal("1000.00"))
                .montoIva(new BigDecimal("160.00"))
                .ivaIncluido(true)
                .interesTotalCargo(BigDecimal.ZERO)
                .interesPago(BigDecimal.ZERO)
                .montoIvaInteres(BigDecimal.ZERO)
                .interesBonificado(BigDecimal.ZERO)
                .totalDescuento(BigDecimal.ZERO)
                .saldoPendienteActual(new BigDecimal("1160.00"))
                .build();

        ContextoFinalizacionOrdenResponse contexto = ContextoFinalizacionOrdenResponse.builder()
                .ordenUuid("b44f8c9c-995f-f111-b368-f4b52065acb8")
                .serieReciboId(912)
                .serieReciboDescripcion("AUTOMATICA")
                .totalPagado(new BigDecimal("1160.00"))
                .lugarPagoId(647)
                .intenciones(List.of(intencion))
                .build();

        AplicarCierreOrdenPayloadDto payload = engine.armarPlanDeLiquidacion(contexto, 912, "LVIVAR");

        assertNotNull(payload);
        PadreActualizarLiquidacionDto padre = payload.padresActualizar().getFirst();
        // Debe abonar $1160 (base + IVA) para liquidar el saldo del padre
        assertEquals(new BigDecimal("1160.00"), padre.montoAbonar());
        assertEquals(new BigDecimal("0.00"), padre.nuevoSaldoPendiente());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el total pagado es insuficiente para cubrir la orden")
    void testValidarFondosInsuficientes() {
        ContextoIntencionOrdenDto intencion = ContextoIntencionOrdenDto.builder()
                .idMovimiento(303)
                .montoCapital(new BigDecimal("1000.00"))
                .montoIva(BigDecimal.ZERO)
                .ivaIncluido(false)
                .interesTotalCargo(BigDecimal.ZERO)
                .interesPago(BigDecimal.ZERO)
                .montoIvaInteres(BigDecimal.ZERO)
                .interesBonificado(BigDecimal.ZERO)
                .totalDescuento(BigDecimal.ZERO)
                .saldoPendienteActual(new BigDecimal("1000.00"))
                .build();

        ContextoFinalizacionOrdenResponse contexto = ContextoFinalizacionOrdenResponse.builder()
                .ordenUuid("c55f8c9c-995f-f111-b368-f4b52065acb9")
                .serieReciboId(912)
                .serieReciboDescripcion("AUTOMATICA")
                .totalPagado(new BigDecimal("500.00")) // Solo pagó 500 de 1000
                .lugarPagoId(647)
                .intenciones(List.of(intencion))
                .build();

        assertThrows(IllegalArgumentException.class, () -> engine.armarPlanDeLiquidacion(contexto, 912, "LVIVAR"));
    }
}
