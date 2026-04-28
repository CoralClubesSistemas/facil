package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.projection.DatosReciboResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.FinalizarOrdenCobranzaResponse;
import com.coralclubes.facil.modules.cobranza.repository.CobranzaRepository;
import com.coralclubes.facil.modules.cobranza.repository.RecibosRepository;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.NotificationClient;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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

            log.debug("Archivos generados para recibo {}: Original ID = {}, Reimpresión ID = {}", recibo.getFolio(), archivos.originalId(), archivos.reimpresionId());

            // 2. Actualizar metadatos digitales en BD
            repository.spCobranzaActualizarMetadatosDigitales(
                    orden.numeroRecibo(),
                    orden.serieReciboId(),
                    archivos.originalId().toString(),
                    archivos.cadenaOriginal(),
                    usuario
            );

            // 3. Enviar a la lista de correos del cliente
            if (correosClientes != null && !correosClientes.isEmpty()) {
                for (String correo : correosClientes) {
                    enviarEmail(correo, "Su Recibo de Pago (Original)", archivos.originalId());
                }
            }

            // 4. Enviar a auditoría
            enviarEmail(correoAuditoria, "Copia de Recibo - Folio: " + recibo.getFolio(), archivos.reimpresionId());

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
            UUID archivo = generador.generarReciboCancelacion(recibo);

            log.debug("Archivo generado para cancelación de recibo {}: ID = {}", recibo.getFolio(), archivo);

            // 2. Actualizar metadatos digitales en BD
            recibosRepository.spCobranzaActualizarCancelacionReciboDigital(
                    recibo.getNumeroRecibo(),
                    recibo.getIdSerieRecibo(),
                    archivo.toString(),
                    usuario
            );

            // 3. Esperar a que el archivo termine de cargarse antes de notificar
            Thread.sleep(3000);

            // 4. Enviar al correo del cliente
            if (correoCliente != null && !correoCliente.isEmpty()) {
                enviarEmail(correoCliente, "Recibo de Cancelación - Folio: " + recibo.getFolio(), archivo);
            }

            // 5. Enviar a auditoría
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

    private void enviarEmail(String destinatario, String asunto, UUID fileId) {
        log.info("Enviando email a {} con asunto '{}' y adjunto ID: {}", destinatario, asunto, fileId);

        SolicitudNotificacionDto solicitud = SolicitudNotificacionDto.builder()
                .codigoSistema("FACIL")
                .aliasConfig("SMTP_GENERAL")
                .destinatarios(List.of(destinatario))
                .cuerpo(asunto)
                .prioridad(10)
                .adjuntos(List.of(fileId.toString()))
                .build();
        notificationClient.enviarNotificacion(solicitud);
    }
}