package com.coralclubes.facil.modules.cobranza.engines.cupones.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

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
}
