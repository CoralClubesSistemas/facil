package com.coralclubes.facil.modules.reservaciones.dto.response;

public record SugerenciaAmenidadDto(
        Integer idArticulo,
        String nombreArticulo,
        String unidadMedida,
        Integer cantidadSugerida
) {}