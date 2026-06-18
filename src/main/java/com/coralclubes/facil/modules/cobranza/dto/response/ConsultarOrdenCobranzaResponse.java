package com.coralclubes.facil.modules.cobranza.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ConsultarOrdenCobranzaResponse(
        UUID uuid,
        Integer numeroOrden,
        String membresia,
        String nombreSocio,
        String correo,
        Integer desarrolloId,
        String desarrolloNombre,
        String estatus,
        String fechaCreacion,
        BigDecimal totalPagar,
        String moneda,
        List<ConceptoOrdenCobranzaDto> conceptos,
        String mensajeAdicional
) {
}

