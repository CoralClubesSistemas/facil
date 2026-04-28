package com.coralclubes.facil.modules.reservaciones.listener;

import com.coralclubes.facil.modules.reservaciones.repository.RecepcionRepository;
import com.coralclubes.facil.modules.reservaciones.service.RecepcionService;
import com.coralclubes.facil.shared.enums.MovimientosEnum;
import com.coralclubes.facil.shared.events.dto.ReciboCanceladoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservacionCancelacionListener {

    private final RecepcionService recepcionService;

    /**
     * Este método escucha el evento de cancelación de recibo.
     * Asegura que el evento se procese solo si la transacción de Cobranza hizo COMMIT.
     */
    @ApplicationModuleListener
    public void handleReciboCancelado(ReciboCanceladoEvent event) {
        log.info("Analizando movimientos cancelados para la membresía: {}", event.membresia());

        event.movimientosAfectados().stream()
                // Si el tipo de movimiento es de reservacion
                .filter(mov -> Objects.equals(mov.tipoMovimiento(), MovimientosEnum.RESERVACIONES.getId()))
                .forEach(mov -> {
                    try {
                        log.info("Detectado movimiento de hotel cancelado (ID: {}). Revirtiendo reserva...", mov.idMovimiento());



                    } catch (Exception e) {
                        log.error("Error revirtiendo reserva para movimiento {}: {}", mov.idMovimiento(), e.getMessage());
                        // Modulith marcará el evento como fallido en la tabla EVENT_PUBLICATION para reintento
                        throw e;
                    }
                });
    }
}