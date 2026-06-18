package com.coralclubes.facil.modules.reservaciones.listener;

import com.coralclubes.facil.modules.reservaciones.service.ReservacionesService;
import com.coralclubes.facil.shared.events.dto.ReservacionConfirmadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservacionesEventListener {

    private final ReservacionesService reservacionesService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void manejarReservacionConfirmada(ReservacionConfirmadaEvent event) {
        try {
            // 1. Generar la carta de ocupación, cargarla a Storage y persistir el UUID en base de datos
            UUID uuid = reservacionesService.generarYPersistirCartaOcupacion(event);

            // 2. Enviar el correo electrónico al usuario con el documento adjunto (UUID)
            reservacionesService.enviarNotificacionCartaOcupacion(event, uuid, List.of());

            log.info("Carta de ocupación generada y notificación enviada exitosamente para folios: {}", event.foliosGenerados());
        } catch (Exception e) {
            log.error("Error during asynchronous occupation letter generation/delivery for folios {}: {}", event.foliosGenerados(), e.getMessage(), e);
        }
    }
}
