package com.coralclubes.facil.modules.reservaciones.dto.response;

public record ReglaAmenidadActualDto(
        Integer idArticulo,
        String nombreArticulo,
        String unidadMedida,
        Integer cantidadBase,
        Integer cantidadPorPersona
) {}