package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.projection.DatosReciboResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.FinalizarOrdenCobranzaResponse;
import com.coralclubes.facil.modules.cobranza.repository.CobranzaRepository;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.NotificationClient;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CobranzaPostProcesoAsyncService {

    private final CobranzaRepository repository;
    private final CobranzaGeneradorDocumentosService generador;
    private final NotificationClient notificationClient;
    private final BusinessLogger log;

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

            log.info(usuario, "Generación de PDF y notificaciones completadas para folio: " + recibo.getFolio());

        } catch (Exception e) {
            log.error(usuario, "Error en post-proceso asíncrono (PDF/Email) para recibo " + orden.numeroRecibo() + ": " + e.getMessage());
        }
    }

    private void enviarEmail(String destinatario, String asunto, UUID fileId) {
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