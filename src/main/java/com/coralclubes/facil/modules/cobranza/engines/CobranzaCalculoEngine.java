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
 * Motor de calculo de descuentos, IVA y totales para la cobranza.
 * <p>
 * Este componente encapsula la lógica de cálculo de descuentos en cascada, prorrateo de descuentos, cálculo de IVA y totales finales.
 * Se utiliza tanto para simular los cálculos en el frontend como para preparar los datos para persistencia en la base de datos.
 * <p>
 * Funcionalidades principales:
 * <ul>
 *     <li>Calcular el porcentaje efectivo acumulado de una lista de porcentajes en cascada.</li>
 *     <li>Simular el cálculo de descuentos e impuestos, devolviendo un desglose detallado para el frontend.</li>
 *     <li>Procesar los movimientos para persistencia, calculando valores de base y de IVA listos para inserción en la base de datos.</li>
 *     <li>Validar que el porcentaje de descuento acumulado no exceda el porcentaje máximo autorizado.</li>
 *     <li>Prorratear descuentos en cascada entre los movimientos de cobranza.</li>
 * </ul>
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
        boolean usarCascada = request.porcentajesDescuentoCascada() != null
                && !request.porcentajesDescuentoCascada().isEmpty();

        BigDecimal porcentajeRealAplicable = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        boolean requiereAutorizacion = false;
        boolean autorizado = false;

        if (usarCascada) {
            porcentajeRealAplicable = calcularPorcentajeEfectivoCascada(request.porcentajesDescuentoCascada());
            requiereAutorizacion = requiereAutorizacion(porcentajeRealAplicable, porcentajeAutorizado);
            validarTopeAutorizado(porcentajeRealAplicable, porcentajeAutorizado, request.usuarioAutoriza());
            autorizado = requiereAutorizacion && request.usuarioAutoriza() != null && !request.usuarioAutoriza().isBlank();
        }

        List<GenerarOrdenCobranzaMovimientoRequest> movimientosAjustados;
        if (usarCascada) {
            movimientosAjustados = prorratearDescuentoCascada(
                    request.movimientos(),
                    porcentajeRealAplicable,
                    null,
                    request.usuarioAutoriza()
            );
        } else {
            movimientosAjustados = request.movimientos();
        }

        boolean agregarIva = Boolean.TRUE.equals(request.agregarIva());
        boolean ivaIncluido = Boolean.TRUE.equals(request.ivaIncluido());

        List<ItemCalculoDescuentoDto> items = new ArrayList<>();
        BigDecimal totalCapitalOriginal = BigDecimal.ZERO;
        BigDecimal totalDescuentoGeneral = BigDecimal.ZERO;
        BigDecimal totalCapitalConDescuento = BigDecimal.ZERO;
        BigDecimal totalIvaGeneral = BigDecimal.ZERO;
        BigDecimal totalFinalGeneral = BigDecimal.ZERO;

        for (int i = 0; i < request.movimientos().size(); i++) {
            GenerarOrdenCobranzaMovimientoRequest original = request.movimientos().get(i);
            GenerarOrdenCobranzaMovimientoRequest ajustado = movimientosAjustados.get(i);

            BigDecimal capitalOriginal = original.montoCapital();
            BigDecimal descuento = ajustado.totalDescuento();
            BigDecimal capitalConDesc = capitalOriginal.subtract(descuento);
            if (capitalConDesc.compareTo(BigDecimal.ZERO) < 0) {
                capitalConDesc = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal interesPago = original.interesPago();
            BigDecimal interesBonif = original.interesesBonificados();

            BigDecimal montoIvaCapital = BigDecimal.ZERO;
            BigDecimal montoIvaInteres = BigDecimal.ZERO;

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

            BigDecimal porcentajeItem = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            if (capitalOriginal.compareTo(BigDecimal.ZERO) > 0) {
                porcentajeItem = descuento.multiply(CIEN).divide(capitalOriginal, 2, RoundingMode.HALF_UP);
            }

            BigDecimal totalPagarItem;
            if (agregarIva && !ivaIncluido) {
                totalPagarItem = capitalConDesc.add(interesPago).add(montoIvaCapital).add(montoIvaInteres);
            } else {
                totalPagarItem = capitalConDesc.add(interesPago);
            }

            items.add(ItemCalculoDescuentoDto.builder()
                    .idMovimiento(original.idMovimiento())
                    .montoCapitalOriginal(capitalOriginal)
                    .porcentajeAplicado(porcentajeItem)
                    .montoDescuento(descuento)
                    .montoCapitalConDescuento(capitalConDesc)
                    .pagoInteresOriginal(interesPago)
                    .interesesBonificados(interesBonif)
                    .montoIva(montoIvaCapital)
                    .montoIvaInteres(montoIvaInteres)
                    .totalPagarItem(totalPagarItem)
                    .build());

            totalCapitalOriginal = totalCapitalOriginal.add(capitalOriginal);
            totalDescuentoGeneral = totalDescuentoGeneral.add(descuento);
            totalCapitalConDescuento = totalCapitalConDescuento.add(capitalConDesc);
            totalIvaGeneral = totalIvaGeneral.add(montoIvaCapital).add(montoIvaInteres);
            totalFinalGeneral = totalFinalGeneral.add(totalPagarItem);
        }

        return SimularCalculoDescuentoResponse.builder()
                .porcentajeRealAplicable(porcentajeRealAplicable)
                .porcentajeAutorizado(porcentajeAutorizado)
                .requiereAutorizacion(requiereAutorizacion)
                .autorizado(autorizado)
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
     */
    public List<MovimientoIntencionPersistenciaDto> procesarParaPersistencia(
            GenerarOrdenCobranzaRequest request,
            BigDecimal porcentajeAutorizado
    ) {
        boolean usarCascada = request.porcentajesDescuentoCascada() != null
                && !request.porcentajesDescuentoCascada().isEmpty();

        List<GenerarOrdenCobranzaMovimientoRequest> movimientosAjustados;

        if (usarCascada) {
            BigDecimal porcentajeReal = calcularPorcentajeEfectivoCascada(request.porcentajesDescuentoCascada());
            validarTopeAutorizado(porcentajeReal, porcentajeAutorizado, request.usuarioAutorizaCascada());

            movimientosAjustados = prorratearDescuentoCascada(
                    request.movimientos(),
                    porcentajeReal,
                    request.justificacionCascada(),
                    request.usuarioAutorizaCascada()
            );
        } else {
            movimientosAjustados = request.movimientos();
        }

        boolean agregarIva = Boolean.TRUE.equals(request.agregarIva());
        boolean ivaIncluido = Boolean.TRUE.equals(request.ivaIncluido());

        List<MovimientoIntencionPersistenciaDto> resultado = new ArrayList<>();

        for (GenerarOrdenCobranzaMovimientoRequest mov : movimientosAjustados) {
            BigDecimal capital = mov.montoCapital();
            BigDecimal descuento = mov.totalDescuento();
            BigDecimal interesPago = mov.interesPago();
            BigDecimal interesTotal = mov.montoInteres();
            BigDecimal interesBonif = mov.interesesBonificados();

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
                    .justificacionDescuento(mov.justificacionDescuento())
                    .usuarioAutoriza(mov.usuarioAutoriza())
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

    private List<GenerarOrdenCobranzaMovimientoRequest> prorratearDescuentoCascada(
            List<GenerarOrdenCobranzaMovimientoRequest> movimientos,
            BigDecimal porcentajeEfectivoTotal,
            String justificacion,
            String usuarioAutoriza
    ) {
        BigDecimal factorEfectivo = porcentajeEfectivoTotal.divide(CIEN, 8, RoundingMode.HALF_UP);

        List<GenerarOrdenCobranzaMovimientoRequest> resultado = new ArrayList<>();
        for (GenerarOrdenCobranzaMovimientoRequest mov : movimientos) {
            BigDecimal descuento = mov.montoCapital()
                    .multiply(factorEfectivo)
                    .setScale(2, RoundingMode.HALF_UP);

            resultado.add(new GenerarOrdenCobranzaMovimientoRequest(
                    mov.idMovimiento(),
                    mov.montoCapital(),
                    mov.montoInteres(),
                    mov.interesPago(),
                    mov.interesesBonificados(),
                    descuento,
                    justificacion != null ? justificacion : mov.justificacionDescuento(),
                    usuarioAutoriza != null ? usuarioAutoriza : mov.usuarioAutoriza()
            ));
        }
        return resultado;
    }
}
