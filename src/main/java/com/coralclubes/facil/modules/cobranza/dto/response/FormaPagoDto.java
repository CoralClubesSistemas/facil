package com.coralclubes.facil.modules.cobranza.dto.response;

public record FormaPagoDto(
        Integer id,
        String clave,
        String descripcion,
        String icono,
        String color
) {
}

