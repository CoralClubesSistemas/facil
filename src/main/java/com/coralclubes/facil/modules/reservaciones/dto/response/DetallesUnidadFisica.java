package com.coralclubes.facil.modules.reservaciones.dto.response;

public record DetallesUnidadFisica(
        Integer idUnidadFisica,
        String numeroUnidadFisica,
        Integer tipoUnidad,
        String nombreTipoUnidad,
        Integer capacidadMaxima,
        Boolean disponible,
        Integer idDesarrollo,
        String nombreDesarrollo,
        Integer piso,
        Integer idEstatus,
        String nombreEstatus,
        Integer idPadre
) {
}
