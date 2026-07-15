package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record MembresiaAccesosFinSemanaDto(
        Integer numeroTrimestre,
        Integer accesosPermitidos,
        Integer accesosUtilizados,
        LocalDateTime ultimaFechaIngreso
) {
}
