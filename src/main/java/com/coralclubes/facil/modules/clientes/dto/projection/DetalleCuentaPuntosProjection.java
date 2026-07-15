package com.coralclubes.facil.modules.clientes.dto.projection;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record DetalleCuentaPuntosProjection(
        Integer numeroPlan,
        String descripcionMovimiento,
        Integer puntosLiberados,
        Integer puntosConsumidos,
        Integer puntosHospedaje,
        Integer puntosInstalaciones,
        Integer puntosCampogolf,
        Integer saldoPuntos,
        String estatusPuntos,
        String numeroAutorizacion,
        String usuario,
        LocalDateTime fechaMovimiento
) {}
