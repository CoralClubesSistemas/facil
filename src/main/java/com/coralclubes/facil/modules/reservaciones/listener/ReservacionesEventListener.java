package com.coralclubes.facil.modules.reservaciones.listener;

import com.coralclubes.facil.modules.reservaciones.service.ReservacionCartaOcupacionAsyncService;
import com.coralclubes.facil.shared.events.dto.ReservacionConfirmadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservacionesEventListener {

    private final ReservacionCartaOcupacionAsyncService cartaOcupacionAsyncService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void manejarReservacionConfirmada(ReservacionConfirmadaEvent event) {
        log.info("Lógica de negocio y cambios en BD confirmados para folios: {}. Despachando generación de carta de ocupación en hilo asíncrono...",
                event.foliosGenerados());
        cartaOcupacionAsyncService.procesarCartaOcupacionAsincrona(event);
    }
}
