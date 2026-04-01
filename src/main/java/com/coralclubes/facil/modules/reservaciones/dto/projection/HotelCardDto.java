package com.coralclubes.facil.modules.reservaciones.dto.projection;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Proyección del SP: spResvObtenerHotelesCard
 * Muestra la información resumida para las tarjetas del grid de hoteles.
 */
@Builder
public record HotelCardDto(
        Integer idDesarrollo,
        String nombreHotel,
        String direccionCompleta,
        String telefono,
        String descripcionCorta,
        BigDecimal calificacion,
        UUID uuidPortada,
        String imagenUrl
) {}
