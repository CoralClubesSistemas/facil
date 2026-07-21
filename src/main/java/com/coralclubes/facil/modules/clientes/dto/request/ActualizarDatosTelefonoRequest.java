package com.coralclubes.facil.modules.clientes.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ActualizarDatosTelefonoRequest(
        String nuevoNumeroTelefono,
        String lada,
        String extensionPrincipal,
        String extensionAlterna,
        @NotNull Integer tipoTelefono,
        String observaciones
) {
}
