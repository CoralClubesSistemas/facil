package com.coralclubes.facil.shared.platform.notifications.domain.repository;

import com.coralclubes.facil.shared.platform.notifications.domain.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByDestinatarioAndEstadoOrderByNivelPrioridadDesc(String destinatario, String estado);
    long countByDestinatarioAndEstado(String destinatario, String estado);
    List<Notificacion> findByDestinatarioAndEstadoOrderByNivelPrioridadDescFechaCreacionDesc(String destinatario, String estado);
}
