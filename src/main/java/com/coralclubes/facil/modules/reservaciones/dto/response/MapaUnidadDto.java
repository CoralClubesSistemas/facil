package com.coralclubes.facil.modules.reservaciones.dto.response;

import java.time.LocalDateTime;

public record MapaUnidadDto(
        Integer unidadId,
        String tipoUnidad,
        String numeroUnidad,
        Integer piso,
        Integer capacidad,
        String estatusClave,
        String estatusDescripcion,

        LocalDateTime fechaEntrada,
        LocalDateTime fechaSalida,

        LocalDateTime fechaBloqueoInicial,
        LocalDateTime fechaBloqueoFinal,
        String razonBloqueo
) {}