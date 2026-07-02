package com.coralclubes.facil.shared.infrastructure.integration.notifications.dto;

import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * DTO que mapea la estructura de la solicitud requerida por el microservicio
 * de notificaciones para enviar una notificación. Contiene información sobre el sistema que envía la notificación,
 * los destinatarios,
 * el asunto,
 * el cuerpo,
 * la plantilla a utilizar,
 * las variables para la plantilla,
 * el remitente,
 * los metadatos adicionales,
 * la prioridad y los adjuntos.
 */
@Builder
public record SolicitudNotificacionDto(
        String aliasConfig,
        List<String> destinatarios,
        String asunto,
        String cuerpo,
        String codigoPlantilla,
        Map<String, Object> variables,
        String remitenteOverride,
        Map<String, Object> metadatos,
        Integer prioridad,
        List<String> adjuntos,
        String password
) {
}