package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record PaqueteAnualMovimientoResponse(
        Integer paqueteAnualMovimientoId,
        Integer movimientoId,
        String movimiento,
        Boolean aplicaDescuento,
        Boolean obligatorio,
        BigDecimal cuotaVigente,
        Integer anioVigenciaCuota,
        Map<String, Object> configuracionAdicional,
        LocalDateTime fechaRegistro,
        String usuarioRegistro
) {}
