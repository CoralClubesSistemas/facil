package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CotizacionCredencialesResponse(
        BigDecimal tarifaEstablecida,
        Integer cantidadBeneficiarios,
        Integer cantidadMovimientosAInsertar,
        Integer cantidadMovimientosAModificar,
        BigDecimal calculoTotal,
        BigDecimal cuotaPorRegitro
) {}
