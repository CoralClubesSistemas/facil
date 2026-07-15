package com.coralclubes.facil.modules.clientes.dto.projection;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record PaquetePuntosPlanProjection(
        String membresia,
        Integer numeroPlan,
        LocalDateTime fechaInicio,
        LocalDateTime finalVigencia,
        Integer puntosMembresia,
        Integer puntosEnganche,
        Integer puntosMensualidades,
        Integer puntosLiberados,
        Integer puntosConsumidos,
        String estatusPlanPuntos
) {}
