package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record CuentaPuntosDto(
        Integer cliId,
        String membresia,
        String nombreSocio,
        Integer numeroPlan,
        LocalDateTime fechaInicio,
        LocalDateTime finalVigencia,
        Integer puntosMembresia,
        Integer puntosEnganche,
        Integer puntosMensualidades,
        String descripcionMovimiento,
        Integer puntosLiberados,
        Integer puntosConsumidos,
        Integer puntosHospedaje,
        Integer puntosInstalaciones,
        Integer puntosCampoGolf,
        Integer saldoPuntos,
        String estatusPuntos,
        LocalDateTime fechaEmision
) {
}
