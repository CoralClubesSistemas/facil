package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.request.EmailRequestDto;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.NotificationClient;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final NotificationClient notificationClient;

    @Value("${app.clients.notifications.aliases.default}")
    private String aliasConfig;

    public void enviarCorreo(EmailRequestDto request) {
        SolicitudNotificacionDto solicitud = SolicitudNotificacionDto.builder()
                .aliasConfig(aliasConfig)
                .destinatarios(request.destinatarios())
                .asunto(request.asunto())
                .codigoPlantilla("email-corporativo-v1")
                .variables(Map.of("cuerpoCorreo", request.cuerpo()))
                .build();

        Map<String, byte[]> archivos = new HashMap<>();
        if (request.adjuntos() != null) {
            for (var adjunto : request.adjuntos()) {
                if (adjunto.nombre() != null && adjunto.contenido() != null) {
                    archivos.put(adjunto.nombre(), adjunto.contenido());
                }
            }
        }

        notificationClient.enviarNotificacionConAdjuntos(solicitud, archivos);
    }
}
