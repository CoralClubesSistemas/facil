package com.coralclubes.facil.modules.cobranza.dto.request;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record AplicarCierreOrdenPayloadDto(
        Integer serieReciboId,
        String serieReciboDescripcion,
        Integer estatusReciboId,
        Integer tipoReciboId,
        BigDecimal importeRecibo,
        List<MovimientoNuevoLiquidacionDto> movimientosNuevos,
        List<PadreActualizarLiquidacionDto> padresActualizar
) {
}
