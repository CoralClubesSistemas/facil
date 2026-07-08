package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;

@Builder
public record ConsumoPuntosPdfItemDto(
        String fechaConsumo,
        String accesoClub,
        String desarrolloUso,
        String tipoCliente,
        String zonaAcceso,
        String periodoAcceso,
        String puntosHospedaje,
        String puntosInstalaciones,
        String puntosCampoGolf,
        String numeroAutorizacion,
        String descripcionReferencia
) {
}
