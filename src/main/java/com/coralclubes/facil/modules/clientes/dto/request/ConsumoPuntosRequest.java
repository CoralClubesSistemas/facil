package com.coralclubes.facil.modules.clientes.dto.request;

import lombok.Builder;

@Builder
public record ConsumoPuntosRequest(
        String membresia,
        Integer desarrolloId,
        Integer totalPuntos,
        Integer puntosHospedaje,
        Integer puntosInstalaciones,
        Integer puntosCampoGolf,
        Integer idMovimiento,
        String descripcion,
        String usuario,
        Integer numBeneficiario,
        Integer idTipoCliente,
        Integer idTipoAcceso,
        Integer idPeriodoUso
) {}