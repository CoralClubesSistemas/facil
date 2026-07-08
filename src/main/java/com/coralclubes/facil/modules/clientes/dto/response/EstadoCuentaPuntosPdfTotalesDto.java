package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;

@Builder
public record EstadoCuentaPuntosPdfTotalesDto(
        String totalPuntosMembresia,
        String totalPuntosEnganche,
        String totalPuntosLiberados,
        String totalPuntosConsumidos,
        String totalPuntosHospedaje,
        String totalPuntosInstalaciones,
        String totalPuntosGolf,
        String saldoPuntosLibres
) {
}
