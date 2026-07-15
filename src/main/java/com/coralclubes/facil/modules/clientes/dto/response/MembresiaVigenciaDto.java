package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record MembresiaVigenciaDto(
        LocalDateTime inicioVigencia,
        Integer vigencia,
        String unidadVigencia,
        LocalDateTime finalVigencia,
        String fechaFinalAmpliacion,
        Integer idConceptoAmpliacion,
        String conceptoAmpliacion
) {
}
