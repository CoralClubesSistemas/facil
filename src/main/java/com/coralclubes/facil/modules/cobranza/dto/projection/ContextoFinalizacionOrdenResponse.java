package com.coralclubes.facil.modules.cobranza.dto.projection;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record ContextoFinalizacionOrdenResponse(
        String ordenUuid,
        Integer numeroOrden,
        String membresia,
        Integer desarrolloId,
        Integer clasificacionMembresia,
        Integer tipoMembresia,
        Integer lugarPagoId,
        Integer serieReciboId,
        String serieReciboDescripcion,
        BigDecimal totalPagado,
        Boolean tieneEfectivo,
        Boolean tieneTarjeta,
        Integer desarrolloUsuario,
        List<ContextoIntencionOrdenDto> intenciones
) {
}
