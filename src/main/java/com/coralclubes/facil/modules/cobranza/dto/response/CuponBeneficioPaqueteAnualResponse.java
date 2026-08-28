package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CuponBeneficioPaqueteAnualResponse(
        Integer cuponId,
        String cupon,
        String nomenclatura,
        Integer cantidadCupones,
        LocalDateTime periodoInicio,
        LocalDateTime periodoFin,
        LocalDateTime inicioVigenciaPeriodo,
        LocalDateTime finVigenciaPeriodo
) {}
