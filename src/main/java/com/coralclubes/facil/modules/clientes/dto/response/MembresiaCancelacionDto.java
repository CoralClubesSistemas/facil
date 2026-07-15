package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record MembresiaCancelacionDto(
        String membresia,
        Integer consecutivo,
        String estatusMembresia,
        String motivoBaja,
        String razonConvenio,
        String usuarioRegistro,
        LocalDateTime fechaRegistro
) {
}
