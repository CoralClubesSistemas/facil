package com.coralclubes.facil.shared.events.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record ReciboPagadoEvent(
        String membresia,
        Integer numeroRecibo,
        Integer serieReciboId,
        Integer tipoMembresia,
        Integer clasificacionMembresia,
        String usuario,
        Integer desarrolloId,
        BigDecimal totalPagado,
        List<MovimientosReciboPagado> movimientosAfectados
) {
    @Builder
    public record MovimientosReciboPagado(
            Integer idMovimiento,
            Integer tipoMovimiento,
            BigDecimal montoPagado,
            Integer estatusId,
            String estatus
    ) {
    }
}
