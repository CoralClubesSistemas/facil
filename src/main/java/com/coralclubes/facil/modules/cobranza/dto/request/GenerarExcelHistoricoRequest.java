package com.coralclubes.facil.modules.cobranza.dto.request;

import lombok.Builder;
import java.util.List;

@Builder
public record GenerarExcelHistoricoRequest(
        String membresia,
        List<String> columnas
) {
}
