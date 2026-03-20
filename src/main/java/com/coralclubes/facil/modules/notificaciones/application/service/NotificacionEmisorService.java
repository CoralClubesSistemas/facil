package com.coralclubes.facil.modules.notificaciones.application.service;

import com.coralclubes.facil.modules.notificaciones.application.dto.NotificacionDto;
import com.coralclubes.facil.modules.notificaciones.application.dto.PeticionNotificacionDto;
import com.coralclubes.facil.modules.notificaciones.domain.model.Notificacion;
import com.coralclubes.facil.modules.notificaciones.domain.repository.NotificacionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionEmisorService {
    private final NotificacionRepository notificacionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Envía una notificación a un solo usuario.
     */
    @Transactional
    public void enviarAUsuario(String remitente, String destinatario, PeticionNotificacionDto dto) {
        Notificacion notificacion = construirEntidad(remitente, destinatario, dto);
        notificacion = notificacionRepository.save(notificacion);
        empujarAWebSocket(notificacion);
    }

    /**
     * Envía la misma notificación a una lista de usuarios (Ideal para promociones o alertas de sistema).
     */
    @Transactional
    public void enviarAMultiples(String remitente, List<String> destinatarios, PeticionNotificacionDto dto) {
        if (destinatarios == null || destinatarios.isEmpty()) return;

        // Construimos una entidad por cada destinatario
        List<Notificacion> notificaciones = destinatarios.stream()
                .map(destinatario -> construirEntidad(remitente, destinatario, dto))
                .toList();

        // Insert Masivo a la base de datos
        List<Notificacion> guardadas = notificacionRepository.saveAll(notificaciones);

        // Disparamos el WebSocket individualmente
        guardadas.forEach(this::empujarAWebSocket);
    }

    private Notificacion construirEntidad(String remitente, String destinatario, PeticionNotificacionDto dto) {
        String metadataJson = null;
        try {
            if (dto.metadata() != null && !dto.metadata().isEmpty()) {
                metadataJson = objectMapper.writeValueAsString(dto.metadata());
            }
        } catch (Exception e) {
            log.error("Error serializando la metadata de la notificación", e);
        }

        return Notificacion.builder()
                .remitenteUsername(remitente)
                .destinatarioUsername(destinatario)
                .tipoMensaje(dto.tipoMensaje())
                .nivelPrioridad(dto.nivelPrioridad() != null ? dto.nivelPrioridad() : 1)
                .titulo(dto.titulo())
                .mensaje(dto.mensaje())
                .metadataJson(metadataJson)
                .build();
    }

    private void empujarAWebSocket(Notificacion notificacion) {
        try {
            // En WS enviamos el mismo contrato que REST: NotificacionDto (metadata como Map).
            NotificacionDto dto = new NotificacionDto(
                    notificacion.getId(),
                    notificacion.getRemitenteUsername(),
                    notificacion.getTipoMensaje(),
                    notificacion.getNivelPrioridad(),
                    notificacion.getTitulo(),
                    notificacion.getMensaje(),
                    parseContenido(notificacion.getMetadataJson()),
                    notificacion.getFechaCreacion(),
                    notificacion.getEstado(),
                    notificacion.getFechaLectura()
            );

            messagingTemplate.convertAndSendToUser(
                    notificacion.getDestinatarioUsername(),
                    "/queue/alertas",
                    dto
            );
        } catch (Exception e) {
            log.warn("El usuario {} no está conectado al WS. La notificación se leerá después.", notificacion.getDestinatarioUsername());
        }
    }

    private java.util.Map<String, Object> parseContenido(String contenidoJson) {
        try {
            if (contenidoJson == null || contenidoJson.isBlank()) return java.util.Map.of();
            return objectMapper.readValue(contenidoJson, new TypeReference<java.util.Map<String, Object>>() {});
        } catch (Exception e) {
            return java.util.Map.of();
        }
    }
}