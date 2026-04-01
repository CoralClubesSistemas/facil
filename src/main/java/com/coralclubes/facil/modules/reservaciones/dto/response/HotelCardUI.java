package com.coralclubes.facil.modules.reservaciones.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record HotelCardUI(
        Integer idDesarrollo,
        String nombreHotel,
        String direccionCompleta,
        String telefono,
        String descripcionCorta,
        BigDecimal calificacion,
        String imagenUrl,
        List<CaracteristicaDto> caracteristicas
) {
}
