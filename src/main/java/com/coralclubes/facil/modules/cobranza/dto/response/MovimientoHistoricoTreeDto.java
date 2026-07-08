package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;
import java.util.List;

@Builder
public record MovimientoHistoricoTreeDto(
        String fechaVencimiento,
        String concepto,
        String montoCargo,
        String montoAbono,
        String montoInteres,
        String montoPendiente,
        String recibo,
        String fechaPago,
        List<MovimientoHistoricoTreeDto> hijos,
        List<MovimientoHistoricoTreeDto> nietos
) {
}
