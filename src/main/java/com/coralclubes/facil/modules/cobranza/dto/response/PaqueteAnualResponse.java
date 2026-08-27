package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PaqueteAnualResponse(
        Integer id,
        Integer desarrolloId,
        String desarrollo,
        Integer anio,
        Integer tipoMembresiaId,
        String tipoMembresia,
        Integer clasificacionMembresiaId,
        String clasificacionMembresia,
        LocalDateTime fechaRegistro,
        String usuarioRegistro
) {}
