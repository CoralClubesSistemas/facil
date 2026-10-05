package com.coralclubes.facil.modules.cobranza.engines;

import com.coralclubes.facil.modules.cobranza.dto.projection.ContextoFinalizacionOrdenResponse;
import com.coralclubes.facil.modules.cobranza.dto.projection.ContextoIntencionOrdenDto;
import com.coralclubes.facil.modules.cobranza.dto.request.AplicarCierreOrdenPayloadDto;
import com.coralclubes.facil.modules.cobranza.dto.request.MovimientoNuevoLiquidacionDto;
import com.coralclubes.facil.modules.cobranza.dto.request.PadreActualizarLiquidacionDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class CobranzaLiquidacionEngine {

    // Catálogo TIPOS_MOVIMIENTOS
    private static final int TMV_PAGO = 4;
    private static final int TMV_INTERES = 6;
    private static final int TMV_DESCUENTO = 8;
    private static final int TMV_BONIFICACION = 54;
    private static final int TMV_IVA = 418;

    // Catálogo ESTATUS_MOVIMIENTOS
    private static final int ESTATUS_MOV_PAGADO = 603;
    private static final int ESTATUS_MOV_PAGO_PARCIAL = 604;
    private static final int ESTATUS_MOV_BONIFICADO = 608;

    // Catálogo ESTATUS_RECIBOS
    private static final int ESTATUS_RECIBO_GENERADO = 684;
    private static final int ESTATUS_RECIBO_PAGADO = 686;

    // Tolerancia para centavos
    private static final BigDecimal TOLERANCIA_CENTAVOS = new BigDecimal("0.01");

    /**
     * Construye el plan de liquidación contable calculando movimientos hijos/nietos
     * y amortizaciones de saldo pendiente libres del bug de IVA.
     */
    public AplicarCierreOrdenPayloadDto armarPlanDeLiquidacion(
            ContextoFinalizacionOrdenResponse contexto,
            Integer tipoSerieRecibo,
            String usuario
    ) {
        // 1. Calcular total orden requerido
        BigDecimal totalOrdenRequerido = BigDecimal.ZERO;
        for (ContextoIntencionOrdenDto item : contexto.intenciones()) {
            BigDecimal subtotalItem = item.montoCapital()
                    .add(item.interesPago())
                    .add(item.montoIva())
                    .add(item.montoIvaInteres())
                    .subtract(item.totalDescuento());
            totalOrdenRequerido = totalOrdenRequerido.add(subtotalItem);
        }
        totalOrdenRequerido = totalOrdenRequerido.setScale(2, RoundingMode.HALF_UP);

        // 2. Validar fondos suficientes
        BigDecimal totalPagado = contexto.totalPagado() != null ? contexto.totalPagado() : BigDecimal.ZERO;
        if (totalPagado.compareTo(totalOrdenRequerido.subtract(TOLERANCIA_CENTAVOS)) < 0) {
            throw new IllegalArgumentException(
                    String.format("Fondos insuficientes. Orden requiere: $%.2f | Pagado: $%.2f",
                            totalOrdenRequerido, totalPagado)
            );
        }

        // 3. Resolver Estatus Final del Recibo
        int estatusReciboFinal = ESTATUS_RECIBO_PAGADO;
        boolean usuarioEsInternet = "INTERNET".equalsIgnoreCase(usuario);
        int desarrolloUsuario = contexto.desarrolloUsuario() != null ? contexto.desarrolloUsuario() : 0;
        boolean tieneTarjeta = Boolean.TRUE.equals(contexto.tieneTarjeta());
        boolean tieneEfectivo = Boolean.TRUE.equals(contexto.tieneEfectivo());

        if (!usuarioEsInternet && desarrolloUsuario == 0 && (tieneTarjeta || tieneEfectivo)) {
            estatusReciboFinal = ESTATUS_RECIBO_GENERADO;
        }

        // 4. Resolver Tipo de Recibo
        int tipoReciboId = resolverTipoRecibo(contexto.lugarPagoId(), tipoSerieRecibo);

        // 5. Construir movimientos nuevos y amortizaciones de padres
        List<MovimientoNuevoLiquidacionDto> movimientosNuevos = new ArrayList<>();
        List<PadreActualizarLiquidacionDto> padresActualizar = new ArrayList<>();

        for (ContextoIntencionOrdenDto item : contexto.intenciones()) {
            Integer raizMvtId = item.idMovimiento();

            // A) PAGO CAPITAL: ImporteCapital - TotalDescuento
            BigDecimal pagoCapital = item.montoCapital().subtract(item.totalDescuento()).setScale(2, RoundingMode.HALF_UP);
            if (pagoCapital.compareTo(BigDecimal.ZERO) > 0) {
                movimientosNuevos.add(MovimientoNuevoLiquidacionDto.builder()
                        .tipoRegistro("PAGO_CAPITAL")
                        .raizMvtId(raizMvtId)
                        .padreRelacionTipo("RAIZ")
                        .importeCargo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                        .importeAbono(pagoCapital)
                        .tipoMovId(TMV_PAGO)
                        .estatusId(ESTATUS_MOV_PAGADO)
                        .concepto("PAGO")
                        .build());
            }

            // B) DESCUENTO
            if (item.totalDescuento().compareTo(BigDecimal.ZERO) > 0) {
                String conceptoDesc = item.justificacionDescuento() != null && !item.justificacionDescuento().isBlank()
                        ? item.justificacionDescuento()
                        : "DESCUENTO";
                movimientosNuevos.add(MovimientoNuevoLiquidacionDto.builder()
                        .tipoRegistro("DESCUENTO")
                        .raizMvtId(raizMvtId)
                        .padreRelacionTipo("RAIZ")
                        .importeCargo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                        .importeAbono(item.totalDescuento())
                        .tipoMovId(TMV_DESCUENTO)
                        .estatusId(ESTATUS_MOV_PAGADO)
                        .concepto(conceptoDesc)
                        .build());
            }

            // C) CARGO DE INTERÉS
            if (item.interesTotalCargo().compareTo(BigDecimal.ZERO) > 0) {
                int estatusCargoInteres = item.interesBonificado().compareTo(item.interesTotalCargo()) >= 0
                        ? ESTATUS_MOV_BONIFICADO
                        : ESTATUS_MOV_PAGADO;

                movimientosNuevos.add(MovimientoNuevoLiquidacionDto.builder()
                        .tipoRegistro("CARGO_INTERES")
                        .raizMvtId(raizMvtId)
                        .padreRelacionTipo("RAIZ")
                        .importeCargo(item.interesTotalCargo())
                        .importeAbono(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                        .tipoMovId(TMV_INTERES)
                        .estatusId(estatusCargoInteres)
                        .concepto("CARGO POR INTERESES MORATORIOS")
                        .build());

                // D) NIETOS DE INTERÉS: Bonificación
                if (item.interesBonificado().compareTo(BigDecimal.ZERO) > 0) {
                    movimientosNuevos.add(MovimientoNuevoLiquidacionDto.builder()
                            .tipoRegistro("BONIF_INTERES")
                            .raizMvtId(raizMvtId)
                            .padreRelacionTipo("CARGO_INTERES")
                            .importeCargo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                            .importeAbono(item.interesBonificado())
                            .tipoMovId(TMV_BONIFICACION)
                            .estatusId(ESTATUS_MOV_BONIFICADO)
                            .concepto("BONIFICACIÓN DE INTERESES")
                            .build());
                }

                // D) NIETOS DE INTERÉS: Pago de interés
                if (item.interesPago().compareTo(BigDecimal.ZERO) > 0) {
                    movimientosNuevos.add(MovimientoNuevoLiquidacionDto.builder()
                            .tipoRegistro("PAGO_INTERES")
                            .raizMvtId(raizMvtId)
                            .padreRelacionTipo("CARGO_INTERES")
                            .importeCargo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                            .importeAbono(item.interesPago())
                            .tipoMovId(TMV_PAGO)
                            .estatusId(ESTATUS_MOV_PAGADO)
                            .concepto("PAGO DE INTERESES")
                            .build());
                }

                // F) CARGO Y PAGO DE IVA DE INTERESES
                if (item.montoIvaInteres().compareTo(BigDecimal.ZERO) > 0) {
                    movimientosNuevos.add(MovimientoNuevoLiquidacionDto.builder()
                            .tipoRegistro("CARGO_IVA_INT")
                            .raizMvtId(raizMvtId)
                            .padreRelacionTipo("CARGO_INTERES")
                            .importeCargo(item.montoIvaInteres())
                            .importeAbono(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                            .tipoMovId(TMV_IVA)
                            .estatusId(ESTATUS_MOV_PAGADO)
                            .concepto("CARGO DE IVA POR INTERESES")
                            .build());

                    movimientosNuevos.add(MovimientoNuevoLiquidacionDto.builder()
                            .tipoRegistro("PAGO_IVA_INT")
                            .raizMvtId(raizMvtId)
                            .padreRelacionTipo("CARGO_IVA_INT")
                            .importeCargo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                            .importeAbono(item.montoIvaInteres())
                            .tipoMovId(TMV_PAGO)
                            .estatusId(ESTATUS_MOV_PAGADO)
                            .concepto("PAGO DE IVA POR INTERESES")
                            .build());
                }
            }

            // E) CARGO Y PAGO DE IVA DE CAPITAL
            if (item.montoIva().compareTo(BigDecimal.ZERO) > 0) {
                movimientosNuevos.add(MovimientoNuevoLiquidacionDto.builder()
                        .tipoRegistro("CARGO_IVA")
                        .raizMvtId(raizMvtId)
                        .padreRelacionTipo("RAIZ")
                        .importeCargo(item.montoIva())
                        .importeAbono(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                        .tipoMovId(TMV_IVA)
                        .estatusId(ESTATUS_MOV_PAGADO)
                        .concepto("CARGO DE IVA")
                        .build());

                movimientosNuevos.add(MovimientoNuevoLiquidacionDto.builder()
                        .tipoRegistro("PAGO_IVA")
                        .raizMvtId(raizMvtId)
                        .padreRelacionTipo("CARGO_IVA")
                        .importeCargo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                        .importeAbono(item.montoIva())
                        .tipoMovId(TMV_PAGO)
                        .estatusId(ESTATUS_MOV_PAGADO)
                        .concepto("PAGO DE IVA")
                        .build());
            }

            // =========================================================================
            // G) CÁLCULO DE AMORTIZACIÓN DEL PADRE (CORRECCIÓN DEFINITIVA DEL BUG)
            // =========================================================================
            // Si IVA Incluido: El cargo original contenía el IVA. Se amortiza Base + IVA.
            // Si IVA Adicionado: El cargo original NO tenía IVA. Solo se amortiza el Capital.
            BigDecimal montoAbonarPadre;
            if (Boolean.TRUE.equals(item.ivaIncluido())) {
                montoAbonarPadre = item.montoCapital().add(item.montoIva()).setScale(2, RoundingMode.HALF_UP);
            } else {
                montoAbonarPadre = item.montoCapital().setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal saldoActual = item.saldoPendienteActual() != null ? item.saldoPendienteActual() : BigDecimal.ZERO;
            BigDecimal nuevoSaldo = saldoActual.subtract(montoAbonarPadre).setScale(2, RoundingMode.HALF_UP);
            if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
                nuevoSaldo = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }

            int nuevoEstatusId = nuevoSaldo.compareTo(TOLERANCIA_CENTAVOS) <= 0
                    ? ESTATUS_MOV_PAGADO
                    : ESTATUS_MOV_PAGO_PARCIAL;

            padresActualizar.add(PadreActualizarLiquidacionDto.builder()
                    .idMovimiento(raizMvtId)
                    .montoAbonar(montoAbonarPadre)
                    .nuevoSaldoPendiente(nuevoSaldo)
                    .nuevoEstatusId(nuevoEstatusId)
                    .build());
        }

        return AplicarCierreOrdenPayloadDto.builder()
                .serieReciboId(contexto.serieReciboId())
                .serieReciboDescripcion(contexto.serieReciboDescripcion())
                .estatusReciboId(estatusReciboFinal)
                .tipoReciboId(tipoReciboId)
                .importeRecibo(totalOrdenRequerido)
                .movimientosNuevos(movimientosNuevos)
                .padresActualizar(padresActualizar)
                .build();
    }

    private int resolverTipoRecibo(Integer lugarPagoId, Integer tipoSerieRecibo) {
        if (lugarPagoId == null) {
            return 1120;
        }
        if (lugarPagoId == 647 && Integer.valueOf(1237).equals(tipoSerieRecibo)) {
            return 1122; // Corporativo + Cargo Automático
        }
        if (lugarPagoId == 647 || lugarPagoId == 648) {
            return 1120; // Corporativo o Desarrollo -> Recibo cobranza interna
        }
        if (lugarPagoId == 1020) {
            return 1123; // Internet -> Recibo cobranza internet
        }
        return 1120;
    }
}
