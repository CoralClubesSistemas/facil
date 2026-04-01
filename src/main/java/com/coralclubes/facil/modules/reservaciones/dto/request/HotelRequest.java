package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Petición para crear o actualizar la información general de un hotel.
 */
@Builder
public record HotelRequest(
        Integer id, // Null para crear, un valor para actualizar

        @NotBlank(message = "El nombre del hotel es obligatorio")
        @Size(max = 200, message = "El nombre no puede exceder los 200 caracteres")
        String nombre,

        @NotBlank(message = "La dirección es obligatoria")
        String direccion,

        String numero,

        @NotBlank(message = "La localidad es obligatoria")
        String localidad,

        @NotBlank(message = "La ciudad es obligatoria")
        String ciudad,

        @NotBlank(message = "El estado es obligatorio")
        String estado,

        @NotBlank(message = "El código postal es obligatorio")
        String codigoPostal,

        String mapaIframe,
        String descripcionCorta,
        String descripcionLarga,

        @Size(max = 50)
        String telefono
) {}