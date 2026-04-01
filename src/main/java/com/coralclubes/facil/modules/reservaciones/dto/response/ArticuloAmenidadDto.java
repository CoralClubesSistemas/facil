package com.coralclubes.facil.modules.reservaciones.dto.response;

public record ArticuloAmenidadDto(
        Integer idArticulo,
        String skuSicofi,
        String nombreArticulo,
        String descripcion,
        String unidadMedida,
        String marca
) {}