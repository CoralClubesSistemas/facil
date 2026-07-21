package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record ResumenReferidosResponse(
        String membresiaReferidor,
        Integer totalReferidos,
        BigDecimal montoAsignadoGlobal,
        BigDecimal montoConsumidoGlobal,
        BigDecimal montoDisponibleGlobal
) {
}
