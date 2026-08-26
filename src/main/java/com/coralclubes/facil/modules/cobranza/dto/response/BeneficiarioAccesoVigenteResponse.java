package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BeneficiarioAccesoVigenteResponse(
        String membresia,
        Integer numBeneficiario,
        Integer estatusAcceso,
        Integer motivo,
        String motivoDescripcion,
        String notaRecomendaciones,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFinal,
        String usuarioRegistra,
        LocalDateTime fechaRegistro
) {}
