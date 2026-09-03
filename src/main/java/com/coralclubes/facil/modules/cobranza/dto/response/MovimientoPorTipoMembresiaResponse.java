package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record MovimientoPorTipoMembresiaResponse(
        Integer id,
        String descripcion,
        Integer periodicidadId,
        String periodicidad,
        Integer baseDeCobroId,
        String baseDeCobro,
        Boolean generaInteres,
        BigDecimal cuota,
        Integer anioVigencia,
        Boolean cuotaForzosa,
        Boolean esValido
) {}
