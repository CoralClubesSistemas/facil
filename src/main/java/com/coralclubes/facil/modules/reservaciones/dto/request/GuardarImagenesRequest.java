package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Envoltorio para guardar las imágenes subidas de un hotel.
 */
public record GuardarImagenesHotelRequest(
        @NotNull(message = "El ID del hotel es obligatorio")
        Integer idHotel,

        @Valid
        List<ImagenHotelRequest> imagenes
) {}