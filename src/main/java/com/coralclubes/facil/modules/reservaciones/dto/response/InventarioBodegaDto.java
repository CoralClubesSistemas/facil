package com.coralclubes.facil.modules.reservaciones.dto.response;

public record InventarioBodegaDto(
        Integer idAlmacen,
        String nombreAlmacen,
        Integer idArticulo,
        String skuSicofi,
        String nombreArticulo,
        String unidadMedida,
        Integer stockActual
) {}
