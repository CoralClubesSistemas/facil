package com.coralclubes.facil.modules.clientes.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ObtenerArchivosNotaResponse(
        String nombreArchivo,
        UUID uuidArchivo,
        String tipoArchivo,
        String urlDescarga,
        String usuarioCarga,
        LocalDateTime fechaCarga
) {
}

