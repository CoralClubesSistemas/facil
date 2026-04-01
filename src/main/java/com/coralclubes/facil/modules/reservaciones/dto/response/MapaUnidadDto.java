package com.coralclubes.facil.modules.reservaciones.dto.response;

public record MapaUnidadDto(
        Integer unidadId,
        String tipoUnidad,
        String numeroUnidad,
        Integer piso,
        Integer capacidad,
        String estatusClave,
        String estatusDescripcion
) {}