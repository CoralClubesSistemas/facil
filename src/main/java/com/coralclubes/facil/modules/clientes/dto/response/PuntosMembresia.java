package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Builder
public record PuntosMembresia(
        String membresia,
        String nombreSocio,
        Integer puntosLiberadosRegulares,
        Integer puntosLiberadosPromocion,
        Integer totalPuntosLiberados,
        Integer puntosConsumidos,
        Integer saldoPuntosNeto,
        Timestamp fechaEmisionReporte
) {
}
