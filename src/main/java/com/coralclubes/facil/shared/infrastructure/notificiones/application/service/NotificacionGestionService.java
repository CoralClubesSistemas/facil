package com.coralclubes.facil.shared.infrastructure.notificiones.application.service;

import com.coralclubes.facil.shared.infrastructure.notificiones.application.dto.NotificacionDto;
import com.coralclubes.facil.shared.infrastructure.notificiones.domain.model.Notificacion;
import com.coralclubes.facil.shared.infrastructure.notificiones.domain.repository.NotificacionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificacionGestionService {

    private final NotificacionRepository notificacionRepository;
    private final ObjectMapper objectMapper;

    // 1. Obtener todas las no leídas al iniciar sesión
    @Transactional(readOnly = true)
    public List<NotificacionDto> obtenerNoLeidas(String username) {
        List<Notificacion> resp = notificacionRepository.findByDestinatarioUsernameAndEstadoOrderByFechaCreacionDesc(username, "NO_LEIDO");

        return resp.stream().map(n -> new NotificacionDto(
                n.getId(),
                n.getRemitenteUsername(),
                n.getTipoMensaje(),
                n.getNivelPrioridad(),
                n.getTitulo(),
                n.getMensaje(),
                parseContenido(n.getMetadataJson()),
                n.getFechaCreacion(),
                n.getEstado(),
                n.getFechaLectura()
        )).toList();
    }

    private Map<String, Object> parseContenido(String contenidoJson) {
        try {
            return objectMapper.readValue(contenidoJson, Map.class);
        } catch (Exception e) {
            return Map.of(); // Retorna un mapa vacío si hay error
        }
    }

    // 2. Obtener solo el contador (para el numero rojo en la campanita)
    @Transactional(readOnly = true)
    public long contarNoLeidas(String username) {
        return notificacionRepository.countByDestinatarioUsernameAndEstado(username, "NO_LEIDO");
    }

    // 3. Marcar una notificación específica como leída (cuando hace clic en ella)
    @Transactional
    public void marcarComoLeida(Long id, String username) {
        notificacionRepository.findById(id).ifPresent(notificacion -> {
            // Validar que la notificación realmente le pertenece a este usuario
            if (notificacion.getDestinatarioUsername().equals(username) && "NO_LEIDO".equals(notificacion.getEstado())) {
                notificacion.setEstado("LEIDO");
                notificacion.setFechaLectura(LocalDateTime.now());
                notificacionRepository.save(notificacion);
            }
        });
    }

    // 4. Marcar todas como leídas
    @Transactional
    public void marcarTodasComoLeidas(String username) {
        List<NotificacionDto> noLeidas = obtenerNoLeidas(username);

        // Obtener las entidades completas para actualizar
        List<Notificacion> notificaciones = noLeidas.stream()
                .map(dto -> notificacionRepository.findById(dto.id()).orElse(null))
                .filter(Objects::nonNull) // Filtrar los que no se encontraron (aunque deberían existir)
                .toList();

        notificaciones.forEach(n -> {
            n.setEstado("LEIDO");
            n.setFechaLectura(LocalDateTime.now());
        });
        notificacionRepository.saveAll(notificaciones);
    }
}