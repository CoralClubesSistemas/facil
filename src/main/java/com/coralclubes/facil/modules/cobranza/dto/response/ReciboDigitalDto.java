package com.coralclubes.facil.modules.cobranza.dto.response;

import java.util.UUID;

public record ReciboDigitalDto(
        UUID original,
        UUID reimpresion,
        UUID cancelado,
        UUID activo
) {}
