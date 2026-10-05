package com.coralclubes.facil.modules.cobranza.dto.projection;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Proyeccion del objeto que se manda como listado a la base de datos
 * para persistencia de los movimientos de intencion de pago.
 */
@Builder
public record MovimientoIntencionPersistenciaDto(
        Integer idMovimiento,
        BigDecimal montoCapital,
        BigDecimal montoIva,
        BigDecimal montoInteresTotal,
        BigDecimal pagoInteres,
        BigDecimal montoIvaInteres,
        BigDecimal interesBonificado,
        BigDecimal totalDescuento,
        String justificacionDescuento,
        String usuarioAutoriza
) {
}
