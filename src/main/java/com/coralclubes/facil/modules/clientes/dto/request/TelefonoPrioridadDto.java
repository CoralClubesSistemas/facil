package com.coralclubes.facil.modules.clientes.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record TelefonoPrioridadDto(
        @NotNull String telefono,
        @NotNull Integer prioridad
) {
}
