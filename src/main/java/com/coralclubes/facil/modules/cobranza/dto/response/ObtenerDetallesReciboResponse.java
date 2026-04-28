package com.coralclubes.facil.modules.cobranza.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ObtenerDetallesReciboResponse(
        String reciboUuid,
        String cadenaSeguridad,
        String estatus,
        String empresa,
        String folio,
        LocalDateTime fecha,
        String membresia,
        String clienteNombre,
        BigDecimal subtotal,
        BigDecimal totalIva,
        BigDecimal descuentoTotal,
        BigDecimal total,
        List<DetalleReciboMovimientoDto> movimientos,
        List<DetalleReciboFormaPagoDto> formasPago,
        String desarrollo,
        String producto
) {
}

