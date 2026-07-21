package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record MembresiaLlamadaResponse(
        Integer id,
        String membresia,
        LocalDateTime fechaRegistro,
        String usuario,
        String extension,
        String telefono,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        Integer duracion
) {
}
