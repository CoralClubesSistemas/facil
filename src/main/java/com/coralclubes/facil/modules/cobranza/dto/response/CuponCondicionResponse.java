package com.coralclubes.facil.modules.cobranza.dto.response;

public record CuponCondicionResponse(
        Integer condicionId,
        String claveCondicion,
        String valorCondicion
) {
}
