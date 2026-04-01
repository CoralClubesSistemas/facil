package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Envoltorio para eliminar imágenes (lote múltiple).
 */
public record EliminarImagenesRequest(
        @NotNull(message = "El ID es obligatorio")
        Integer id,

        @Valid
        List<EliminarImagenRequest> imagenesAEliminar
) {}
