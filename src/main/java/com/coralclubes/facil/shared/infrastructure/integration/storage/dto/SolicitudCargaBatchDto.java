package com.coralclubes.facil.shared.infrastructure.integration.storage.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SolicitudCargaBatchDto(
        @NotEmpty(message = "La lista de solicitudes no puede estar vacía")
        @Size(max = 50, message = "No se pueden solicitar más de 50 firmas por lote")
        List<@Valid SolicitudCargaDto> solicitudes
) {}