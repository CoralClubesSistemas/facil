package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record MovimientoHistoricoDto(
        Long id,
        String numeroPlan,
        String idTipoMovimiento,
        String tipoMovimiento,
        LocalDateTime fechaGeneracion,
        LocalDateTime fechaVencimiento,
        BigDecimal importeCargo,
        BigDecimal importeAbono,
        BigDecimal importePendiente,
        String usuario,
        String estatus,
        Integer idDesarrolloConsumo,
        String desarrolloConsumo,
        String conceptoDescripcion,
        String descripcionMovimiento,
        Integer numeroRecibo,
        Integer idSerieRecibo,
        String folioRecibo,
        Timestamp fechaPagoRecibo,
        Integer cantidadMovimientosFamilia
) {
}
