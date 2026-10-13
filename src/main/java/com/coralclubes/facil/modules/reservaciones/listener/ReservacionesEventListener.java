package com.coralclubes.facil.modules.reservaciones.listener;

import com.coralclubes.facil.modules.reservaciones.dto.response.ResultadoQrReservacion;
import com.coralclubes.facil.modules.reservaciones.service.ReservacionesDocumentosService;
import com.coralclubes.facil.shared.events.dto.ReservacionConfirmadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

/**
 * Listener encargado de procesar la generación y envío de cartas de ocupación y códigos QR oficiales,
 * ejecutándose de forma asíncrona una vez que la transacción en BD de la reservación ha sido confirmada.
 * Coordina explícitamente los 3 procesos:
 * 1. Generación de QR oficial.
 * 2. Generación y persistencia de Carta de Ocupación (PDF).
 * 3. Envío de notificación por correo.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservacionesEventListener {

    private final ReservacionesDocumentosService documentosService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void procesarCartaOcupacion(ReservacionConfirmadaEvent event) {
        log.info("Iniciando procesamiento asíncrono en listener [{}] para folios: {}",
                Thread.currentThread().getName(), event.foliosGenerados());
        try {
            // Reconstruimos el evento desde la base de datos para asegurar que refleje los datos definitivos
            // (por ejemplo, ampliaciones de fechas por cupones, peticiones especiales y montos finales ya confirmados)
            ReservacionConfirmadaEvent eventoFinal = event;
            if (event.foliosGenerados() != null && !event.foliosGenerados().isEmpty() && event.membresia() != null) {
                Integer folioPrincipal = event.foliosGenerados().getFirst();
                ReservacionConfirmadaEvent eventoDb = documentosService.construirEventDesdeDb(event.membresia(), folioPrincipal);

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

            // 1. Proceso de Generación del Código QR oficial
            ResultadoQrReservacion qrResultado = documentosService.generarQrReservacion(eventoFinal);

            // 2. Proceso de Generación y Persistencia de la Carta de Ocupación en PDF con el QR vinculado
            UUID uuidPdf = documentosService.generarYPersistirCartaOcupacion(eventoFinal, qrResultado.qrCodeDataUri());

            // 3. Proceso de Envío de Notificación por correo adjuntando los UUIDs del PDF y del QR
            documentosService.enviarNotificacionCartaOcupacion(
                    eventoFinal,
                    uuidPdf,
                    qrResultado.qrFileId(),
                    List.of()
            );

            log.info("Flujo asíncrono completado exitosamente: QR y Carta de Ocupación generados y notificación enviada para folios: {}",
                    eventoFinal.foliosGenerados());
        } catch (Exception e) {
            log.error("Error durante el procesamiento de carta de ocupación para folios {}: {}",
                    event.foliosGenerados(), e.getMessage(), e);
        }
    }
}
