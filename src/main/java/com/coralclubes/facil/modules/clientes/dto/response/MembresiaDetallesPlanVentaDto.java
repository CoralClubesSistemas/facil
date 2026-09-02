package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record MembresiaDetallesPlanVentaDto(
        Integer numeroPlan,
        Integer idEstatusPlan,
        String estatusPlan,
        BigDecimal precioPlan,
        BigDecimal descuento,
        BigDecimal porcentajeDescuento,
        BigDecimal montoNeto,
        BigDecimal enganche,
        BigDecimal porcentajeEnganche,
        BigDecimal intereses,
        BigDecimal porcentajeIntereses,
        BigDecimal saldo,
        Integer numeroMensualidades,
        BigDecimal importeMensualidades,
        Integer mensualidadesGeneradas,
        Integer mensualidadesPendientes,
        BigDecimal importeUltimaMensualidad,
        LocalDateTime inicioMensualidades,
        LocalDateTime fechaVenta,
        Integer periodicidadMantenimientoId,
        String periodicidadMantenimiento
) {
}
