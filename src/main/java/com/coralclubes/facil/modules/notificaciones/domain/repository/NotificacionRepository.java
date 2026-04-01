package com.coralclubes.facil.modules.notificaciones.domain.repository;

import com.coralclubes.facil.modules.notificaciones.domain.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByDestinatarioUsernameAndEstadoOrderByFechaCreacionDesc(String destinatario, String estado);
    long countByDestinatarioUsernameAndEstado(String destinatario, String estado);
}