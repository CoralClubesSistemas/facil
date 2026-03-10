package com.coralclubes.facil.modules.reservaciones.dto.request;

import java.util.List;

public record TabuladorPuntosRequest(
        Integer desarrolloId,
        Integer rhdtId,
        Integer puntosXNoche,
        List<Integer> diasValidos,
        Boolean finDeSemana,
        Boolean entreSemana
) {}