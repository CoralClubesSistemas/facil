package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;

@Builder
public record ReciboClienteDto(
        String folioRecibo,
        Integer numeroRecibo,
        Integer serieRecibo
) {
}
