package com.coralclubes.facil.modules.cobranza.dto.projection;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ContextoIntencionOrdenDto(
        Integer idMovimiento,
        BigDecimal montoCapital,
        BigDecimal montoIva,
        Boolean ivaIncluido,
        BigDecimal interesTotalCargo,
        BigDecimal interesPago,
        BigDecimal montoIvaInteres,
        BigDecimal interesBonificado,
        BigDecimal totalDescuento,
        String justificacionDescuento,
        String usuarioAutoriza,
        Integer movimientoOriginalId,
        Integer movimientoFamiliaId,
        Integer numeroPlan,
        BigDecimal saldoPendienteActual,
        Integer numeroBeneficiarios
) {
}
