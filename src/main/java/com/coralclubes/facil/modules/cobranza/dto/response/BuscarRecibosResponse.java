package com.coralclubes.facil.modules.cobranza.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO para la búsqueda de recibos de cobranza.
 * Mapea directamente el resultado del SP spCobranzaBuscarRecibos.
 */
public record BuscarRecibosResponse(
        String membresia,
        Integer numeroRecibo,
        Integer serieReciboId,
        String serieReciboDescripcion,
        String folioRecibo,
        String clienteNombre,
        LocalDateTime fechaGeneracion,
        LocalDateTime fechaPago,
        BigDecimal importeRecibo,
        String usuario,
        Integer estatusReciboId,
        String estatusReciboDescripcion,
        Integer desarrolloId,
        String desarrolloDescripcion,
        String tipoMembresia
) {
}


