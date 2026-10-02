package com.coralclubes.facil.modules.cobranza.engines.cupones.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Resultado de la liquidación de un cupón.
 *
 * @param esValido              Indica si el cupón es válido o no.
 * @param mensajeRechazo        Mensaje de rechazo en caso de que el cupón no sea válido.
 * @param montoOriginal         Monto original del cupón antes de aplicar descuentos.
 * @param montoDescuento        Monto de descuento aplicado al cupón.
 * @param montoFinal            Monto final después de aplicar descuentos.
 * @param accionesInstrucciones Lista de acciones e instrucciones resultantes de la aplicación del cupón.
 */
public record CuponLiquidacionResult(
        boolean esValido,
        String mensajeRechazo,
        BigDecimal montoOriginal,
        BigDecimal montoDescuento,
        BigDecimal montoFinal,
        List<CuponAccionInstruccion> accionesInstrucciones
) {
    public CuponLiquidacionResult {
        if (montoOriginal == null) {
            montoOriginal = BigDecimal.ZERO;
        }
        if (montoDescuento == null) {
            montoDescuento = BigDecimal.ZERO;
        }
        if (montoFinal == null) {
            montoFinal = montoOriginal.subtract(montoDescuento).max(BigDecimal.ZERO);
        }
        if (accionesInstrucciones == null) {
            accionesInstrucciones = Collections.emptyList();
        }
    }

    /**
     * Crea un resultado de liquidación para un cupón rechazado.
     *
     * @param montoOriginal El monto original del cupón.
     * @param mensaje       El mensaje de rechazo.
     * @return Un nuevo objeto CuponLiquidacionResult con los valores correspondientes.
     */
    public static CuponLiquidacionResult rechazado(BigDecimal montoOriginal, String mensaje) {
        return new CuponLiquidacionResult(
                false,
                mensaje,
                montoOriginal,
                BigDecimal.ZERO,
                montoOriginal,
                Collections.emptyList()
        );
    }

    /**
     * Crea un resultado de liquidación para un cupón aprobado.
     *
     * @param montoOriginal  El monto original del cupón.
     * @param montoDescuento El monto de descuento aplicado al cupón.
     * @param acciones       Las acciones e instrucciones resultantes de la aplicación del cupón.
     * @return Un nuevo objeto CuponLiquidacionResult con los valores correspondientes.
     */
    public static CuponLiquidacionResult aprobado(
            BigDecimal montoOriginal,
            BigDecimal montoDescuento,
            List<CuponAccionInstruccion> acciones
    ) {
        BigDecimal seguroDescuento = montoDescuento != null ? montoDescuento : BigDecimal.ZERO;
        BigDecimal montoFinal = (montoOriginal != null ? montoOriginal : BigDecimal.ZERO)
                .subtract(seguroDescuento)
                .max(BigDecimal.ZERO);

        return new CuponLiquidacionResult(
                true,
                null,
                montoOriginal,
                seguroDescuento,
                montoFinal,
                acciones != null ? acciones : Collections.emptyList()
        );
    }
}
