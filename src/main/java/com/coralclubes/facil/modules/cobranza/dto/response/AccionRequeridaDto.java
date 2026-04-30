package com.coralclubes.facil.modules.cobranza.dto.response;

public record AccionRequeridaDto(
        String codigoDecision, // Ej: "CANCELAR_RESERVA"
        String mensajeUI,      // Ej: "¿Desea cancelar la reservación asociada?"
        Boolean requerido      // true = obligatorio responder, false = opcional
) {}