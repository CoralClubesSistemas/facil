package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.projection.DatosReciboResponse;
import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.*;
import com.coralclubes.facil.modules.cobranza.repository.CobranzaRepository;
import com.coralclubes.facil.modules.usuarios.service.UsuarioService;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.NotificationClient;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.responses.codes.GeneralResponseCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CobranzaService {

    private final CobranzaRepository repository;
    private final ObjectMapper objectMapper;
    private final CobranzaGeneradorDocumentosService generador;
    private final NotificationClient notificationClient;
    private final BusinessLogger log;
    private final UsuarioService usuarioService;

    public ApiResponse<GenerarOrdenCobranzaResponse> generarOrdenCobranza(GenerarOrdenCobranzaRequest request, String usuario) {
        String movimientosJson = serializarMovimientos(request);

        GenerarOrdenCobranzaResponse result = repository
                .spCobranzaGenerarOrdenCobranza(request.membresia(), usuario, movimientosJson)
                .orElseThrow(() -> new IllegalStateException("No se pudo generar la orden de cobranza."));

        return ApiResponse.success("Orden de cobranza generada correctamente.", result);
    }

    public ApiResponse<ConsultarOrdenCobranzaResponse> consultarOrdenCobranza(UUID ordenUuid) {
        String ordenJson = repository
                .spFacilConsultarOrdenCobranzaJson(ordenUuid)
                .orElseThrow(() -> new IllegalStateException("No se encontró información de la orden de cobranza."));

        if (ordenJson.isBlank()) {
            throw new IllegalStateException("La consulta de orden de cobranza regresó un JSON vacío.");
        }

        try {
            ConsultarOrdenCobranzaResponse response = objectMapper.readValue(ordenJson, ConsultarOrdenCobranzaResponse.class);
            return ApiResponse.success("Orden de cobranza consultada correctamente.", response);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo interpretar el JSON de la orden de cobranza.");
        }
    }

    public ApiResponse<List<FormaPagoDto>> obtenerFormasDePago() {
        return ApiResponse.success("Formas de pago obtenidas correctamente.", repository.spCobranzaCatalogoFormasDePago());
    }

    private String serializarMovimientos(GenerarOrdenCobranzaRequest request) {
        try {
            return objectMapper.writeValueAsString(request.movimientos());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el detalle de movimientos para la orden.");
        }
    }

    public FinalizarOrdenCobranzaResponse finalizarOrdenDeCobranza(String ordenUuid, Integer tipoSerieRecibo, String usuario) {
        return repository.spCobranzaFinalizarOrdenYGenerarRecibo(ordenUuid, tipoSerieRecibo, usuario)
                .orElseThrow(() -> new IllegalArgumentException("Error en el cierre de la orden de cobranza, intente más tarde"));
    }

    public DatosReciboResponse datosRecibo(Integer numeroRecibo, Integer serieReciboId) {
        String json = repository.spCobranzaObtenerDatosRecibo(numeroRecibo, serieReciboId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontraron datos para el recibo solicitado."));

        try {
            return objectMapper.readValue(json, DatosReciboResponse.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo interpretar el JSON de los datos del recibo.");
        }
    }

    // Modificación en CobranzaService.java
    public ApiResponse<String> finalizarOrdenYGenerarRecibo(
            String ordenUuid,
            Integer tipoSerieRecibo,
            String usuario,
            String correo
    ) {
        String correoAuditoria = usuarioService.obtenerCorreoUsuario(usuario).orElse("facil@coralclubes.com");

        // 1. Ejecutar transacción en SQL
        FinalizarOrdenCobranzaResponse orden = finalizarOrdenDeCobranza(ordenUuid, tipoSerieRecibo, usuario);

        // 2. Obtener datos procesados (Aquí el SP ya nos da el estatus inicial 'ORIGINAL')
        DatosReciboResponse recibo = datosRecibo(orden.numeroRecibo(), orden.serieReciboId());

        try {
            // 3. Generar documentos (Original y Reimpresión para auditoría)
            CobranzaGeneradorDocumentosService.ResultadoRecibos archivos = generador.generarAmbosRecibos(recibo);

            // 4. Actualizar metadatos digitales con el ID del original y la cadena generada
            repository.spCobranzaActualizarMetadatosDigitales(
                    orden.numeroRecibo(),
                    orden.serieReciboId(),
                    archivos.originalId().toString(),
                    archivos.cadenaOriginal(),
                    usuario // Usuario que finalizó la orden
            );

            // 5. Envío de Notificaciones
            enviarEmail(correo, "Su Recibo de Pago (Original)", archivos.originalId());
            enviarEmail(correoAuditoria, "Copia de Recibo - Folio: " + recibo.getFolio(), archivos.reimpresionId());

            return ApiResponse.success("Proceso completado exitosamente.", archivos.originalId().toString());

        } catch (Exception e) {
            log.error(usuario, "Error en post-procesamiento de recibo: {}", e.getMessage());
            return ApiResponse.error(GeneralResponseCode.SERVICE_UNAVAILABLE, "Pago aplicado, pero hubo un error con los documentos.");
        }
    }

    // Método auxiliar de envío
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

    public void cancelarOrdenCobranzaSinPago(String uuid) {
        repository.spCobranzaCancelarOrdenCobranzaSinPago(uuid);
    }
}
