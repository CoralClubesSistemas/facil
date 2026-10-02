package com.coralclubes.facil.modules.cobranza.engines.cupones.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Resultado de la aplicación de un beneficio. Devuelto directamente por la estrategia de beneficio,
 * contiene el monto de descuento aplicado y las acciones e instrucciones resultantes de la aplicación del beneficio.
 *
 * @param montoDescuento        El monto de descuento aplicado.
 * @param accionesInstrucciones Las acciones e instrucciones resultantes de la aplicación del beneficio.
 */
public record ResultadoAplicacionBeneficio(
        BigDecimal montoDescuento,
        List<CuponAccionInstruccion> accionesInstrucciones
) {
    public ResultadoAplicacionBeneficio {
        if (montoDescuento == null) {
            montoDescuento = BigDecimal.ZERO;
        }
        if (accionesInstrucciones == null) {
            accionesInstrucciones = Collections.emptyList();
        }
    }

    public static ResultadoAplicacionBeneficio soloDescuento(BigDecimal monto) {
        return new ResultadoAplicacionBeneficio(monto, Collections.emptyList());
    }

    public static ResultadoAplicacionBeneficio soloAcciones(List<CuponAccionInstruccion> acciones) {
        return new ResultadoAplicacionBeneficio(BigDecimal.ZERO, acciones);
    }

    public static ResultadoAplicacionBeneficio noAplica() {
        return new ResultadoAplicacionBeneficio(BigDecimal.ZERO, Collections.emptyList());
    }
}
