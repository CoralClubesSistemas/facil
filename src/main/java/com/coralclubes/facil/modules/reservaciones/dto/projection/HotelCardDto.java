package com.coralclubes.facil.modules.reservaciones.dto.projection;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record HotelCard(
        Integer id,
        String nombre,
        String direccionCompleta,
        String telefono,
        String descripcionCorta,
        BigDecimal calificacion,
        String imagenUrl
) {
}
