package com.coralclubes.facil.modules.cobranza.dto.response;

import com.coralclubes.facil.shared.domain.dto.ArchivoDescarga;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CuponImagenFormatoResponse(
        Integer formatoId,
        Integer cuponId,
        String formatoNombre,
        UUID formatoUuid,
        String configuracionJson,
        String metadataJson,
        LocalDateTime fechaRegistro,
        String usuarioRegistro,
        ArchivoDescarga archivoDescarga
) {
}
