package com.coralclubes.facil.modules.reservaciones.listener;

import com.coralclubes.facil.modules.reservaciones.dto.request.DatosCartaOcupacionDto;
import com.coralclubes.facil.shared.events.dto.ReservacionConfirmadaEvent;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.NotificationClient;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import com.coralclubes.facil.shared.infrastructure.pdf.service.PdfGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservacionesEventListener {

    private final PdfGeneratorService pdfGeneratorService;
    private final NotificationClient notificationClient;

    @Value("${app.clients.notifications.templates.reserva-creada}")
    private String templateReservaCreada;

    @Value("${app.clients.notifications.aliases.aws-ses}")
    private String aliasAwsSes;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void manejarReservacionConfirmada(ReservacionConfirmadaEvent event) {
        log.info("Starting asynchronous occupation letter generation for reservation folios: {}", event.foliosGenerados());
        try {
            generarYEnviarCartaOcupacion(event);
        } catch (Exception e) {
            log.error("Error during asynchronous occupation letter generation/delivery for folios {}: {}", event.foliosGenerados(), e.getMessage(), e);
        }
    }

    private void generarYEnviarCartaOcupacion(ReservacionConfirmadaEvent event) {
        // 1. Formatear la lista de habitaciones para el DTO del PDF
        List<DatosCartaOcupacionDto.HabitacionCartaDto> habitacionesPdf = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (var hab : event.habitaciones()) {
            habitacionesPdf.add(DatosCartaOcupacionDto.HabitacionCartaDto.builder()
                    .tipoHabitacion(hab.tipoHabitacion())
                    .totalPax(hab.totalPersonas())
                    .build());
        }

        String foliosStr = event.foliosGenerados().toString().replace("[", "").replace("]", "");

        // 2. Construir DTO del Generador
        DatosCartaOcupacionDto datosPdf = DatosCartaOcupacionDto.builder()
                .fechaEmision(LocalDate.now().format(formatter))
                .titular(event.nombreReserva())
                .membresia(event.membresia() != null ? event.membresia() : "PÚBLICO GENERAL")
                .foliosReservacion(foliosStr)
                .habitaciones(habitacionesPdf)
                .observaciones(event.peticionEspecial())
                .importeTotal(event.subtotal())
                .fechaEntrada(event.fechaEntrada().format(formatter))
                .fechaSalida(event.fechaSalida().format(formatter))
                .desarrollo(event.desarrollo())
                .build();

        // 3. Generar la Carta de Ocupación localmente
        DocumentoCartaOcupacion doc = generarCartaOcupacion(datosPdf);

        // 4. Preparar Destinatarios
        List<String> destinatarios = new ArrayList<>();
        destinatarios.add(event.email());
        if (event.email2() != null && !event.email2().isBlank()) {
            destinatarios.add(event.email2());
        }

        // 5. Construir Solicitud a Coral Notificaciones
        SolicitudNotificacionDto solicitudNotificacion = SolicitudNotificacionDto.builder()
                .aliasConfig(aliasAwsSes)
                .destinatarios(destinatarios)
                .codigoPlantilla(templateReservaCreada)
                .remitenteOverride("reservaciones@lvivardev.com")
                .variables(Map.of(
                        "nombreUsuario", event.nombreReserva(),
                        "numeroReserva", foliosStr
                ))
                .prioridad(10)
                .build();

        // 6. Enviar por correo con adjunto directo
        notificationClient.enviarNotificacionConAdjuntos(solicitudNotificacion, Map.of(doc.nombreArchivo(), doc.pdfBytes()));
    }

    private record DocumentoCartaOcupacion(byte[] pdfBytes, String nombreArchivo) {}

    private DocumentoCartaOcupacion generarCartaOcupacion(DatosCartaOcupacionDto datos) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("fechaEmision", datos.fechaEmision());
        variables.put("titular", datos.titular());
        variables.put("membresia", datos.membresia() != null ? datos.membresia() : "PÚBLICO GENERAL");
        variables.put("foliosReservacion", datos.foliosReservacion());
        variables.put("habitaciones", datos.habitaciones());
        variables.put("observaciones", datos.observaciones() != null ? datos.observaciones() : "Sin observaciones adicionales.");
        java.text.DecimalFormat df = new java.text.DecimalFormat("$#,##0.00");
        variables.put("importeTotal", df.format(datos.importeTotal()));
        variables.put("fechaEntrada", datos.fechaEntrada());
        variables.put("fechaSalida", datos.fechaSalida());
        variables.put("desarrollo", datos.desarrollo());

        byte[] pdfBytes = pdfGeneratorService.generarPdfDesdeHtml("CARTA_OCUPACION", variables);

        String foliosLimpio = datos.foliosReservacion().replace(" ", "").replace(",", "_");
        String nombreArchivo = "CARTA_OCUPACION_" + foliosLimpio + ".pdf";

        return new DocumentoCartaOcupacion(pdfBytes, nombreArchivo);
    }
}
