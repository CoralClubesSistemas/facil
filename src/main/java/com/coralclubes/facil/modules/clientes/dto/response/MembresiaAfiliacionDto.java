package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record MembresiaAfiliacionDto(
        String membresia,
        String diasDeCorte,
        String plantillaCargoAutomatico,
        String numeroTarjetaEnmascarada,
        String vigenciaTarjeta,
        String banco,
        String prioridad,
        LocalDateTime fechaRegistro,
        String usuarioRegistro
) {
}
