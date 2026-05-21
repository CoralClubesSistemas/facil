package com.coralclubes.facil.shared.infrastructure.integration.storage.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record SolicitudDescargaBatchDto(
        @NotEmpty(message = "Debe proporcionar al menos un UUID")
        @Size(max = 100, message = "Máximo 100 archivos por petición de descarga en lote")
        List<UUID> archivos
) {}