package com.coralclubes.facil.modules.clientes.dto.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public record ArchivoNotaProjection(
        String nombreArchivo,
        UUID uuidArchivo,
        String tipoArchivo,
        String usuarioCarga,
        LocalDateTime fechaCarga
) {
}

