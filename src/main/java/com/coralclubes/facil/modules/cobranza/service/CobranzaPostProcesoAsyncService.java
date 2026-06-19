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
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class CobranzaPostProcesoAsyncService {
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
            UUID original = generador.generarYCargarPdfRecibo(recibo, "ORIGINAL", generarCadenaSeguridad(recibo, "ORIGINAL"));
            UUID reimpresion = generador.generarYCargarPdfRecibo(recibo, "REIMPRESION", generarCadenaSeguridad(recibo, "REIMPRESION"));

            log.debug("Archivos generados para recibo {}: Original ID = {}, Reimpresión ID = {}", recibo.getFolio(), original, reimpresion);

            // 2. Actualizar metadatos digitales en BD
            recibosRepository.spCobranzaActualizarMetadatosDigitales(
                    orden.membresia(),
                    orden.numeroRecibo(),
                    orden.serieReciboId(),
                    String.valueOf(original),
                    String.valueOf(reimpresion),
                    generarCadenaSeguridad(recibo, "ORIGINAL"),
                    usuario
            );

            // 3. Enviar a la lista de correos del cliente con adjunto directo
            if (correosClientes != null && !correosClientes.isEmpty()) {
                for (String correo : correosClientes) {
                    enviarEmail(correo, "Recibo de Pago", original);
                }
            }

            // 4. Enviar a auditoría con adjunto directo
            enviarEmail(correoAuditoria, "Copia de Recibo - Folio: " + recibo.getFolio(), reimpresion);

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
            // 1. Generar documentos (PDF de cancelación), cargarlo a Storage y persistir el UUID en base de datos
            UUID fileId = generador.generarYCargarPdfRecibo(recibo, "CANCELACION", generarCadenaSeguridad(recibo, "CANCELACION"));

            log.debug("Archivo generado para cancelación de recibo {}: ID = {}", recibo.getFolio(), fileId);

            // 2. Actualizar metadatos digitales en BD
            recibosRepository.spCobranzaActualizarCancelacionReciboDigital(
                    recibo.getMembresia(),
                    recibo.getNumeroRecibo(),
                    recibo.getIdSerieRecibo(),
                    String.valueOf(fileId)
            );

            // 4. Enviar al correo del cliente con adjunto directo
            if (correoCliente != null && !correoCliente.isEmpty()) {
                enviarEmail(correoCliente, "Recibo de Cancelación - Folio: " + recibo.getFolio(), fileId);
            }

            // 5. Enviar a auditoría con adjunto directo
            enviarEmail(correoAuditoria, "Copia de Recibo Cancelado - Folio: " + recibo.getFolio(), fileId);

            logger.info(usuario, "Generación de PDF de cancelación y notificaciones completadas para folio: " + recibo.getFolio());

        } catch (Exception e) {
            logger.error(
                    usuario,
                    "Error en post-proceso asíncrono (PDF/Email) para cancelación de recibo " + recibo.getFolio(),
                    e
            );
        }
    }

    private void enviarEmail(String destinatario, String asunto, UUID file) {
        log.info("Enviando email a {} con asunto '{}' y adjunto directo", destinatario, asunto);

        SolicitudNotificacionDto solicitud = SolicitudNotificacionDto.builder()
                .aliasConfig("SMTP_GENERAL")
                .destinatarios(List.of(destinatario))
                .asunto(asunto)
                .cuerpo("Estimado cliente,\n\nAdjunto encontrará el documento relacionado con su recibo.\n\nSaludos cordiales,\nEquipo de Cobranza")
                .prioridad(10)
                .adjuntos(List.of(String.valueOf(file)))
                .build();
        
        notificationClient.enviarNotificacion(solicitud);
    }

    public String generarCadenaSeguridad(DatosReciboResponse recibo, String tipo) {
        String concatenacionValores = tipo.toUpperCase() +
                recibo.getFolio() +
                recibo.getFecha() +
                recibo.getMembresia() +
                recibo.getTotal();

        String hashSignatura = DigestUtils.sha256Hex(concatenacionValores).substring(0, 12);

        return String.format("||%s|%s|%s|%s|%s|%s||",
                tipo.toUpperCase(),
                recibo.getFolio(),
                recibo.getFecha(),
                recibo.getMembresia(),
                recibo.getTotal(),
                hashSignatura.toUpperCase()
        );
    }
}