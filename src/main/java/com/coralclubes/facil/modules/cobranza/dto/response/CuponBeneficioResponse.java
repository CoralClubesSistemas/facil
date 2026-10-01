package com.coralclubes.facil.modules.cobranza.dto.response;

public record CuponBeneficioResponse(
        Integer beneficioId,
        String claveBeneficio,
        String configuracionBeneficio,
        Integer conceptoId,
        String concepto
) {
}
