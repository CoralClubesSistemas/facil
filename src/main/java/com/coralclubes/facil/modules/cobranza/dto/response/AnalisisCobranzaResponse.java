package com.coralclubes.facil.modules.cobranza.dto.response;

public record AnalisisCobranzaResponse(
        String clasificacionRiesgo,
        String justificacionAnalisis,
        String mensajeWhatsappRecomendado
) {}