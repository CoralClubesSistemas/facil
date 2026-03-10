package com.coralclubes.facil.shared.infrastructure.notificiones.application.service;

import com.coralclubes.facil.shared.infrastructure.notificiones.domain.model.Notificacion;
import com.coralclubes.facil.shared.infrastructure.notificiones.domain.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionGestionService {

    private final NotificacionRepository notificacionRepository;

    // 1. Obtener todas las no leídas al iniciar sesión
    @Transactional(readOnly = true)
    public List<Notificacion> obtenerNoLeidas(String username) {
        return notificacionRepository.findByDestinatarioUsernameAndEstadoOrderByFechaCreacionDesc(username, "NO_LEIDO");
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
        List<Notificacion> noLeidas = obtenerNoLeidas(username);
        noLeidas.forEach(n -> {
            n.setEstado("LEIDO");
            n.setFechaLectura(LocalDateTime.now());
        });
        notificacionRepository.saveAll(noLeidas);
    }
}