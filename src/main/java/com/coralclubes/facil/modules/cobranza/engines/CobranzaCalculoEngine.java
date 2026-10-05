package com.coralclubes.facil.modules.cobranza.engines;

import com.coralclubes.facil.modules.cobranza.dto.projection.MovimientoIntencionPersistenciaDto;
import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.SimularCalculoDescuentoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.ItemCalculoDescuentoDto;
import com.coralclubes.facil.modules.cobranza.dto.response.SimularCalculoDescuentoResponse;
import com.coralclubes.facil.shared.infrastructure.exceptions.custom.PercentageExceeded;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Motor de cálculo de descuentos, IVA y totales para la cobranza.
 * Soporta descuentos en cascada a nivel global de solicitud y a nivel individual de movimiento.
 */
@Component
public class CobranzaCalculoEngine {

    private static final BigDecimal TASA_IVA = new BigDecimal("0.16");
    private static final BigDecimal FACTOR_IVA_INCLUIDO = new BigDecimal("1.16");
    private static final BigDecimal CIEN = new BigDecimal("100.00");

    /**
     * Calcula el porcentaje efectivo acumulado de una lista de porcentajes en cascada:
     * 1 - ((1 - p1) * (1 - p2) * ... * (1 - pn))
     */
    public BigDecimal calcularPorcentajeEfectivoCascada(List<BigDecimal> porcentajes) {
        if (porcentajes == null || porcentajes.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal remanente = BigDecimal.ONE;
        for (BigDecimal pct : porcentajes) {
            if (pct != null && pct.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal factor = pct.divide(CIEN, 8, RoundingMode.HALF_UP);
                remanente = remanente.multiply(BigDecimal.ONE.subtract(factor));
            }
        }

        return BigDecimal.ONE.subtract(remanente)
                .multiply(CIEN)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Simula el cálculo de descuentos e impuestos devolviendo el desglose detallado para el frontend.
     */
    public SimularCalculoDescuentoResponse simularCalculo(
            SimularCalculoDescuentoRequest request,
            BigDecimal porcentajeAutorizado
    ) {
        boolean tieneCascadaGlobal = request.porcentajesDescuentoCascada() != null
                && !request.porcentajesDescuentoCascada().isEmpty();

        BigDecimal porcentajeGlobal = tieneCascadaGlobal
                ? calcularPorcentajeEfectivoCascada(request.porcentajesDescuentoCascada())
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        boolean agregarIva = Boolean.TRUE.equals(request.agregarIva());
        boolean ivaIncluido = Boolean.TRUE.equals(request.ivaIncluido());

        List<ItemCalculoDescuentoDto> items = new ArrayList<>();
        BigDecimal totalCapitalOriginal = BigDecimal.ZERO;
        BigDecimal totalDescuentoGeneral = BigDecimal.ZERO;
        BigDecimal totalCapitalConDescuento = BigDecimal.ZERO;
        BigDecimal totalIvaGeneral = BigDecimal.ZERO;
        BigDecimal totalFinalGeneral = BigDecimal.ZERO;

        boolean alMenosUnoRequiereAutorizacion = false;
        boolean todosAutorizados = true;

        for (GenerarOrdenCobranzaMovimientoRequest mov : request.movimientos()) {
            BigDecimal capitalOriginal = mov.montoCapital();
            totalCapitalOriginal = totalCapitalOriginal.add(capitalOriginal);

            // 1. Determinar porcentaje y descuento aplicable al movimiento
            BigDecimal porcentajeItem;
            BigDecimal descuento;
            String usuarioAutorizaItem;

            boolean tieneCascadaIndividual = mov.porcentajesDescuentoCascada() != null
                    && !mov.porcentajesDescuentoCascada().isEmpty();

            if (tieneCascadaIndividual) {
                porcentajeItem = calcularPorcentajeEfectivoCascada(mov.porcentajesDescuentoCascada());
                descuento = capitalOriginal.multiply(porcentajeItem.divide(CIEN, 8, RoundingMode.HALF_UP))
                        .setScale(2, RoundingMode.HALF_UP);
                usuarioAutorizaItem = mov.usuarioAutoriza() != null && !mov.usuarioAutoriza().isBlank()
                        ? mov.usuarioAutoriza()
                        : request.usuarioAutoriza();
            } else if (tieneCascadaGlobal) {
                porcentajeItem = porcentajeGlobal;
                descuento = capitalOriginal.multiply(porcentajeGlobal.divide(CIEN, 8, RoundingMode.HALF_UP))
                        .setScale(2, RoundingMode.HALF_UP);
                usuarioAutorizaItem = request.usuarioAutoriza();
            } else {
                descuento = mov.totalDescuento() != null ? mov.totalDescuento() : BigDecimal.ZERO;
                porcentajeItem = capitalOriginal.compareTo(BigDecimal.ZERO) > 0
                        ? descuento.multiply(CIEN).divide(capitalOriginal, 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                usuarioAutorizaItem = mov.usuarioAutoriza() != null && !mov.usuarioAutoriza().isBlank()
                        ? mov.usuarioAutoriza()
                        : request.usuarioAutoriza();
            }

            // Validar autorización del ítem
            boolean itemRequiereAutorizacion = requiereAutorizacion(porcentajeItem, porcentajeAutorizado);
            boolean itemAutorizado = itemRequiereAutorizacion
                    && usuarioAutorizaItem != null
                    && !usuarioAutorizaItem.isBlank();

            if (itemRequiereAutorizacion) {
                alMenosUnoRequiereAutorizacion = true;
                if (!itemAutorizado) {
                    todosAutorizados = false;
                    validarTopeAutorizado(porcentajeItem, porcentajeAutorizado, usuarioAutorizaItem);
                }
            }

            // 2. Aplicar descuento sobre el capital
            BigDecimal capitalConDesc = capitalOriginal.subtract(descuento);
            if (capitalConDesc.compareTo(BigDecimal.ZERO) < 0) {
                capitalConDesc = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal interesPago = mov.interesPago() != null ? mov.interesPago() : BigDecimal.ZERO;
            BigDecimal interesBonif = mov.interesesBonificados() != null ? mov.interesesBonificados() : BigDecimal.ZERO;

            // 3. Impuestos
            BigDecimal montoIvaCapital = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            BigDecimal montoIvaInteres = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            if (agregarIva) {
                if (ivaIncluido) {
                    BigDecimal baseCapitalNeto = capitalConDesc.divide(FACTOR_IVA_INCLUIDO, 2, RoundingMode.HALF_UP);
                    montoIvaCapital = capitalConDesc.subtract(baseCapitalNeto).setScale(2, RoundingMode.HALF_UP);

                    BigDecimal baseInteresPago = interesPago.divide(FACTOR_IVA_INCLUIDO, 2, RoundingMode.HALF_UP);
                    montoIvaInteres = interesPago.subtract(baseInteresPago).setScale(2, RoundingMode.HALF_UP);
                } else {
                    montoIvaCapital = capitalConDesc.multiply(TASA_IVA).setScale(2, RoundingMode.HALF_UP);
                    montoIvaInteres = interesPago.multiply(TASA_IVA).setScale(2, RoundingMode.HALF_UP);
                }
            }

            BigDecimal totalPagarItem;
            if (agregarIva && !ivaIncluido) {
                totalPagarItem = capitalConDesc.add(interesPago).add(montoIvaCapital).add(montoIvaInteres);
            } else {
                totalPagarItem = capitalConDesc.add(interesPago);
            }

            items.add(ItemCalculoDescuentoDto.builder()
                    .idMovimiento(mov.idMovimiento())
                    .montoCapitalOriginal(capitalOriginal)
                    .porcentajeAplicado(porcentajeItem)
                    .montoDescuento(descuento)
                    .montoCapitalConDescuento(capitalConDesc)
                    .pagoInteresOriginal(interesPago)
                    .interesesBonificados(interesBonif)
                    .montoIva(montoIvaCapital)
                    .montoIvaInteres(montoIvaInteres)
                    .totalPagarItem(totalPagarItem)
                    .requiereAutorizacion(itemRequiereAutorizacion)
                    .autorizado(itemAutorizado)
                    .build());

            totalDescuentoGeneral = totalDescuentoGeneral.add(descuento);
            totalCapitalConDescuento = totalCapitalConDescuento.add(capitalConDesc);
            totalIvaGeneral = totalIvaGeneral.add(montoIvaCapital).add(montoIvaInteres);
            totalFinalGeneral = totalFinalGeneral.add(totalPagarItem);
        }

        // Porcentaje real ponderado sobre el total
        BigDecimal porcentajeRealPonderado = totalCapitalOriginal.compareTo(BigDecimal.ZERO) > 0
                ? totalDescuentoGeneral.multiply(CIEN).divide(totalCapitalOriginal, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        return SimularCalculoDescuentoResponse.builder()
                .porcentajeRealAplicable(porcentajeRealPonderado)
                .porcentajeAutorizado(porcentajeAutorizado)
                .requiereAutorizacion(alMenosUnoRequiereAutorizacion)
                .autorizado(alMenosUnoRequiereAutorizacion && todosAutorizados)
                .montoTotalOriginal(totalCapitalOriginal)
                .montoTotalDescuento(totalDescuentoGeneral)
                .montoTotalConDescuento(totalCapitalConDescuento)
                .montoTotalIva(totalIvaGeneral)
                .montoTotalFinal(totalFinalGeneral)
                .items(items)
                .build();
    }

    /**
     * Procesa los movimientos calculando valores de base y de IVA listos para inserción en el SP.
     * Soporta descuentos en cascada tanto globales como por ítem individual.
     */
    public List<MovimientoIntencionPersistenciaDto> procesarParaPersistencia(
            GenerarOrdenCobranzaRequest request,
            BigDecimal porcentajeAutorizado
    ) {
        boolean tieneCascadaGlobal = request.porcentajesDescuentoCascada() != null
                && !request.porcentajesDescuentoCascada().isEmpty();

        BigDecimal porcentajeGlobal = tieneCascadaGlobal
                ? calcularPorcentajeEfectivoCascada(request.porcentajesDescuentoCascada())
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        boolean agregarIva = Boolean.TRUE.equals(request.agregarIva());
        boolean ivaIncluido = Boolean.TRUE.equals(request.ivaIncluido());

        List<MovimientoIntencionPersistenciaDto> resultado = new ArrayList<>();

        for (GenerarOrdenCobranzaMovimientoRequest mov : request.movimientos()) {
            BigDecimal capital = mov.montoCapital();
            BigDecimal descuento;
            String usuarioAutorizaItem;
            String justificacionItem;

            boolean tieneCascadaIndividual = mov.porcentajesDescuentoCascada() != null
                    && !mov.porcentajesDescuentoCascada().isEmpty();

            if (tieneCascadaIndividual) {
                BigDecimal pctIndividual = calcularPorcentajeEfectivoCascada(mov.porcentajesDescuentoCascada());
                descuento = capital.multiply(pctIndividual.divide(CIEN, 8, RoundingMode.HALF_UP))
                        .setScale(2, RoundingMode.HALF_UP);

                usuarioAutorizaItem = mov.usuarioAutoriza() != null && !mov.usuarioAutoriza().isBlank()
                        ? mov.usuarioAutoriza()
                        : request.usuarioAutorizaCascada();

                justificacionItem = mov.justificacionDescuento() != null && !mov.justificacionDescuento().isBlank()
                        ? mov.justificacionDescuento()
                        : request.justificacionCascada();

                validarTopeAutorizado(pctIndividual, porcentajeAutorizado, usuarioAutorizaItem);
            } else if (tieneCascadaGlobal) {
                descuento = capital.multiply(porcentajeGlobal.divide(CIEN, 8, RoundingMode.HALF_UP))
                        .setScale(2, RoundingMode.HALF_UP);

                usuarioAutorizaItem = request.usuarioAutorizaCascada();
                justificacionItem = request.justificacionCascada() != null
                        ? request.justificacionCascada()
                        : mov.justificacionDescuento();

                validarTopeAutorizado(porcentajeGlobal, porcentajeAutorizado, usuarioAutorizaItem);
            } else {
                descuento = mov.totalDescuento() != null ? mov.totalDescuento() : BigDecimal.ZERO;
                usuarioAutorizaItem = mov.usuarioAutoriza();
                justificacionItem = mov.justificacionDescuento();

                BigDecimal pctDirecto = capital.compareTo(BigDecimal.ZERO) > 0
                        ? descuento.multiply(CIEN).divide(capital, 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                validarTopeAutorizado(pctDirecto, porcentajeAutorizado, usuarioAutorizaItem);
            }

            BigDecimal interesPago = mov.interesPago() != null ? mov.interesPago() : BigDecimal.ZERO;
            BigDecimal interesTotal = mov.montoInteres() != null ? mov.montoInteres() : BigDecimal.ZERO;
            BigDecimal interesBonif = mov.interesesBonificados() != null ? mov.interesesBonificados() : BigDecimal.ZERO;

            BigDecimal montoIva = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            BigDecimal montoIvaInteres = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            if (agregarIva) {
                if (ivaIncluido) {
                    BigDecimal capitalNeto = capital.subtract(descuento);
                    BigDecimal baseCapitalNeto = capitalNeto.divide(FACTOR_IVA_INCLUIDO, 2, RoundingMode.HALF_UP);
                    montoIva = capitalNeto.subtract(baseCapitalNeto).setScale(2, RoundingMode.HALF_UP);

                    BigDecimal baseInteresPago = interesPago.divide(FACTOR_IVA_INCLUIDO, 2, RoundingMode.HALF_UP);
                    montoIvaInteres = interesPago.subtract(baseInteresPago).setScale(2, RoundingMode.HALF_UP);

                    // Bases gravables desglosadas
                    capital = capital.divide(FACTOR_IVA_INCLUIDO, 2, RoundingMode.HALF_UP);
                    descuento = descuento.divide(FACTOR_IVA_INCLUIDO, 2, RoundingMode.HALF_UP);
                    interesPago = baseInteresPago;
                    interesTotal = interesTotal.divide(FACTOR_IVA_INCLUIDO, 2, RoundingMode.HALF_UP);
                    interesBonif = interesBonif.divide(FACTOR_IVA_INCLUIDO, 2, RoundingMode.HALF_UP);
                } else {
                    montoIva = capital.subtract(descuento).multiply(TASA_IVA).setScale(2, RoundingMode.HALF_UP);
                    montoIvaInteres = interesPago.multiply(TASA_IVA).setScale(2, RoundingMode.HALF_UP);
                }
            }

            resultado.add(MovimientoIntencionPersistenciaDto.builder()
                    .idMovimiento(mov.idMovimiento())
                    .montoCapital(capital)
                    .montoIva(montoIva)
                    .montoInteresTotal(interesTotal)
                    .pagoInteres(interesPago)
                    .montoIvaInteres(montoIvaInteres)
                    .interesBonificado(interesBonif)
                    .totalDescuento(descuento)
                    .justificacionDescuento(justificacionItem)
                    .usuarioAutoriza(usuarioAutorizaItem)
                    .build());
        }

        return resultado;
    }

    public boolean requiereAutorizacion(BigDecimal porcentajeReal, BigDecimal porcentajeAutorizado) {
        return porcentajeAutorizado != null && porcentajeReal.compareTo(porcentajeAutorizado) > 0;
    }

    private void validarTopeAutorizado(BigDecimal porcentajeReal, BigDecimal porcentajeAutorizado, String usuarioAutoriza) {
        if (requiereAutorizacion(porcentajeReal, porcentajeAutorizado)) {
            if (usuarioAutoriza == null || usuarioAutoriza.isBlank()) {
                throw new PercentageExceeded(
                        String.format("El descuento acumulado (%.2f%%) excede el porcentaje máximo autorizado (%.2f%%) y requiere un usuario que autorice.",
                                porcentajeReal, porcentajeAutorizado)
                );
            }
        }
    }
}
