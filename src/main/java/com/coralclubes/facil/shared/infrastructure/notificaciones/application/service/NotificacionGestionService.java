package com.coralclubes.facil.shared.infrastructure.notificaciones.application.service;

import com.coralclubes.facil.shared.infrastructure.notificaciones.application.dto.NotificacionDto;
import com.coralclubes.facil.shared.infrastructure.notificaciones.domain.model.Notificacion;
import com.coralclubes.facil.shared.infrastructure.notificaciones.domain.repository.NotificacionRepository;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificacionGestionService {

    private final NotificacionRepository notificacionRepository;
    private final ObjectMapper objectMapper;
    private final UserContext userContext;

    // 1. Obtener todas las no leídas al iniciar sesión
    @Transactional(readOnly = true)
    public List<NotificacionDto> obtenerNoLeidas() {
        String username = userContext.getUsername();

        List<Notificacion> resp = notificacionRepository.findByDestinatarioAndEstadoOrderByFechaCreacionDesc(username, "NO_LEIDO");

        return resp.stream().map(n -> new NotificacionDto(
                n.getId(),
                n.getRemitente(),
                n.getTipoMensaje(),
                n.getNivelPrioridad(),
                n.getTitulo(),
                n.getMensaje(),
                parseContenido(n.getMetadataJson()),
                n.getFechaCreacion(),
                n.getEstado()
        )).toList();
    }

    // 2. Obtener solo el contador (para el numero rojo en la campanita)
    @Transactional(readOnly = true)
    public long contarNoLeidas() {
        String username = userContext.getUsername();

        return notificacionRepository.countByDestinatarioAndEstado(username, "NO_LEIDO");
    }

    // 3. Marcar una notificación específica como leída (cuando hace clic en ella)
    @Transactional
    public void marcarComoLeida(Long id) {
        String username = userContext.getUsername();

        notificacionRepository.findById(id).ifPresent(notificacion -> {
            // Validar que la notificación realmente le pertenece a este usuario
            if (notificacion.getDestinatario().equals(username) && "NO_LEIDO".equals(notificacion.getEstado())) {
                notificacion.setEstado("LEIDO");
                notificacion.setFechaLectura(LocalDateTime.now());
                notificacionRepository.save(notificacion);
            }
        });
    }

    // 4. Marcar todas como leídas
    @Transactional
    public void marcarTodasComoLeidas() {
        String username = userContext.getUsername();

        List<Notificacion> notificaciones = notificacionRepository
                .findByDestinatarioAndEstadoOrderByFechaCreacionDesc(username, "NO_LEIDO");

        if (notificaciones == null || notificaciones.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();
        notificaciones.forEach(n -> {
            n.setEstado("LEIDO");
            n.setFechaLectura(now);
        });

        notificacionRepository.saveAll(notificaciones);
    }

    /* HELPER PARA PARSEAR STRING A JSON */
    private Map<String, Object> parseContenido(String contenidoJson) {
        try {
            return objectMapper.readValue(contenidoJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Map.of(); // Retorna un mapa vacío si hay error
        }
    }
}