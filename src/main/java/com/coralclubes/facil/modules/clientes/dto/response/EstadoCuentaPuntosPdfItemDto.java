package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;

@Builder
public record EstadoCuentaPuntosPdfItemDto(
        String planVenta,
        String inicioVigencia,
        String finVigencia,
        String puntosMembresia,
        String puntosEnganche,
        String puntosMensualidades,
        String descripcionMovimiento,
        String puntosLiberados,
        String puntosConsumidos,
        String puntosHospedaje,
        String puntosInstalaciones,
        String puntosGolf,
        String saldoPuntosLibres,
        String estatusPuntos
) {
}
