package com.coralclubes.facil.modules.cobranza.engines.cupones.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

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
