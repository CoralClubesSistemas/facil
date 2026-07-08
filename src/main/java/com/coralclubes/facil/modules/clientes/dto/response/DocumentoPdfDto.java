package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;

@Builder
public record DocumentoPdfDto(
        String nombre,
        String contenido
) {
}
