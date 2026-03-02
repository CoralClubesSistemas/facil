package com.coralclubes.facil.modules.reservaciones.dto.projection;

import lombok.Builder;

/**
 * Proyección del SP: spResvObtenerHotelImagenes
 * Devuelve la galería de imágenes del hotel.
 */
@Builder
public record ImagenDto(
        Integer idImagen,
        String urlImagen,
        boolean esPortada,
        Integer orden
) {}