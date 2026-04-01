package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Proyección del SP: spResvObtenerHotelDetalles
 * Muestra toda la información para llenar el formulario de edición en el Front.
 */
@Builder
public record HotelDetalleDto(
        Integer idDesarrollo,
        String nombreHotel,
        String direccion,
        String numero,
        String localidad,
        String ciudad,
        String estado,
        String codigoPostal,
        String mapaIframe,
        String telefono,
        String descripcionCorta,
        String descripcionLarga,
        BigDecimal calificacion,
        String direccionCompleta
) {}