package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.facil.shared.events.dto.ReservacionConfirmadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Servicio encargado del procesamiento asíncrono para la generación y envío
 * de cartas de ocupación, garantizando que el hilo se ejecute únicamente tras la
 * confirmación completa de la lógica de negocio y transacción en base de datos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservacionCartaOcupacionAsyncService {

    private final ReservacionesService reservacionesService;

    @Async
    public void procesarCartaOcupacionAsincrona(ReservacionConfirmadaEvent event) {
        log.info("Iniciando procesamiento asíncrono en hilo [{}] para carta de ocupación de folios: {}",
                Thread.currentThread().getName(), event.foliosGenerados());
        try {
            // Reconstruimos el evento desde la base de datos para asegurar que refleje los datos definitivos
            // (por ejemplo, ampliaciones de fechas por cupones, peticiones especiales y montos finales ya confirmados)
            ReservacionConfirmadaEvent eventoFinal = event;
            if (event.foliosGenerados() != null && !event.foliosGenerados().isEmpty() && event.membresia() != null) {
                Integer folioPrincipal = event.foliosGenerados().getFirst();
                ReservacionConfirmadaEvent eventoDb = reservacionesService.construirEventDesdeDb(event.membresia(), folioPrincipal);

                eventoFinal = ReservacionConfirmadaEvent.builder()
                        .nombreReserva(eventoDb.nombreReserva() != null ? eventoDb.nombreReserva() : event.nombreReserva())
                        .email(event.email() != null && !event.email().isBlank() ? event.email() : eventoDb.email())
                        .email2(event.email2())
                        .peticionEspecial(eventoDb.peticionEspecial() != null ? eventoDb.peticionEspecial() : event.peticionEspecial())
                        .membresia(eventoDb.membresia())
                        .fechaEntrada(eventoDb.fechaEntrada())
                        .fechaSalida(eventoDb.fechaSalida()) // Fecha definitiva con ampliaciones en BD
                        .desarrollo(eventoDb.desarrollo())
                        .subtotal(event.subtotal())
                        .foliosGenerados(event.foliosGenerados())
                        .habitaciones(event.habitaciones())
                        .build();
            }

            // 1. Generar la carta de ocupación, cargarla a Storage y persistir el UUID en base de datos
            UUID uuid = reservacionesService.generarYPersistirCartaOcupacion(eventoFinal);

            // 2. Enviar el correo electrónico al usuario con el documento adjunto (UUID)
            reservacionesService.enviarNotificacionCartaOcupacion(eventoFinal, uuid, List.of());

            log.info("Carta de ocupación generada y notificación enviada exitosamente para folios: {}", eventoFinal.foliosGenerados());
        } catch (Exception e) {
            log.error("Error durante la generación/envío asíncrono de carta de ocupación para folios {}: {}",
                    event.foliosGenerados(), e.getMessage(), e);
        }
    }
}
