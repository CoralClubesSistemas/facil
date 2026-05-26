package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.projection.DatosReciboResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.FinalizarOrdenCobranzaResponse;
import com.coralclubes.facil.modules.cobranza.repository.CobranzaRepository;
import com.coralclubes.facil.modules.cobranza.repository.RecibosRepository;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.NotificationClient;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class CobranzaPostProcesoAsyncService {

    private final CobranzaRepository repository;
    private final RecibosRepository recibosRepository;
    private final CobranzaGeneradorDocumentosService generador;
    private final NotificationClient notificationClient;
    private final BusinessLogger logger;

    @Async
    public void procesarDocumentosYNotificaciones(
            FinalizarOrdenCobranzaResponse orden,
            DatosReciboResponse recibo,
            String usuario,
            List<String> correosClientes,
            String correoAuditoria) {

        try {
            // 1. Generar documentos (Original y Reimpresión para auditoría)
            CobranzaGeneradorDocumentosService.ResultadoRecibos archivos = generador.generarAmbosRecibos(recibo);

            log.debug("Archivos generados para recibo {}: Original ID = {}, Reimpresión ID = {}", recibo.getFolio(), archivos.original().fileId(), archivos.reimpression().fileId());

            // 2. Actualizar metadatos digitales en BD
            repository.spCobranzaActualizarMetadatosDigitales(
                    orden.numeroRecibo(),
                    orden.serieReciboId(),
                    archivos.original().fileId().toString(),
                    archivos.cadenaOriginal(),
                    usuario
            );

            // 3. Enviar a la lista de correos del cliente con adjunto directo
            if (correosClientes != null && !correosClientes.isEmpty()) {
                for (String correo : correosClientes) {
                    enviarEmail(correo, "Su Recibo de Pago (Original)", archivos.original());
                }
            }

            // 4. Enviar a auditoría con adjunto directo
            enviarEmail(correoAuditoria, "Copia de Recibo - Folio: " + recibo.getFolio(), archivos.reimpression());

            logger.info(usuario, "Generación de PDF y notificaciones completadas para folio: " + recibo.getFolio());

        } catch (Exception e) {
            logger.error(
                    usuario,
                    "Error en post-proceso asíncrono (PDF/Email) para recibo " + orden.numeroRecibo(),
                    e
            );
        }
    }

    @Async
    public void procesarReciboCanceladoYNotificar(
            DatosReciboResponse recibo,
            String usuario,
            String correoCliente,
            String correoAuditoria) {

        try {
            // 1. Generar documentos (Original y Reimpresión para auditoría)
            CobranzaGeneradorDocumentosService.ReciboGenerado archivo = generador.generarReciboCancelacion(recibo);

            log.debug("Archivo generado para cancelación de recibo {}: ID = {}", recibo.getFolio(), archivo.fileId());

            // 2. Actualizar metadatos digitales en BD
            recibosRepository.spCobranzaActualizarCancelacionReciboDigital(
                    recibo.getNumeroRecibo(),
                    recibo.getIdSerieRecibo(),
                    archivo.fileId().toString(),
                    usuario
            );

            // 3. Esperar a que el archivo termine de cargarse antes de notificar
            Thread.sleep(3000);

            // 4. Enviar al correo del cliente con adjunto directo
            if (correoCliente != null && !correoCliente.isEmpty()) {
                enviarEmail(correoCliente, "Recibo de Cancelación - Folio: " + recibo.getFolio(), archivo);
            }

            // 5. Enviar a auditoría con adjunto directo
            enviarEmail(correoAuditoria, "Copia de Recibo Cancelado - Folio: " + recibo.getFolio(), archivo);

            logger.info(usuario, "Generación de PDF de cancelación y notificaciones completadas para folio: " + recibo.getFolio());

        } catch (Exception e) {
            logger.error(
                    usuario,
                    "Error en post-proceso asíncrono (PDF/Email) para cancelación de recibo " + recibo.getFolio(),
                    e
            );
        }
    }

    private void enviarEmail(String destinatario, String Asunto, CobranzaGeneradorDocumentosService.ReciboGenerado recibo) {
        log.info("Enviando email a {} con asunto '{}' y adjunto directo", destinatario, Asunto);

        SolicitudNotificacionDto solicitud = SolicitudNotificacionDto.builder()
                .aliasConfig("SMTP_GENERAL")
                .destinatarios(List.of(destinatario))
                .cuerpo(Asunto)
                .prioridad(10)
                .build();
        
        notificationClient.enviarNotificacionConAdjuntos(solicitud, Map.of(recibo.nombreArchivo(), recibo.pdfBytes()));
    }
}