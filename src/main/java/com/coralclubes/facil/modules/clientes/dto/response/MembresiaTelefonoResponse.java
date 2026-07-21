package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record MembresiaTelefonoResponse(
        String membresia,
        String numeroTelefono,
        String lada,
        String extensionPrincipal,
        String extensionAlterna,
        String tipoTelefono,
        String estatusTelefono,
        String prioridadTelefono,
        String observaciones,
        String usuarioRegistra,
        LocalDateTime fechaRegistro,
        LocalDateTime fechaUltimaActualizacion,
        String usuarioUltimaActualizacion
) {
}
