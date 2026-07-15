package com.coralclubes.facil.modules.clientes.dto.response;

import com.coralclubes.facil.modules.clientes.dto.projection.PaquetePuntosPlanProjection;
import lombok.Builder;
import java.util.List;

@Builder
public record PaquetesPuntosPlanResponse(
        List<PaquetePuntosPlanProjection> paquetes,
        Integer totalPuntosMembresia,
        Integer totalPuntosLiberados,
        Integer totalPuntosConsumidos,
        Integer saldo
) {}
