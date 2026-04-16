package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.projection.DatosReciboResponse;
import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.*;
import com.coralclubes.facil.modules.cobranza.repository.CobranzaRepository;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.NotificationClient;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
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

        System.out.println("JSON de datos del recibo: " + json); // Log para depuración

        try {
            return objectMapper.readValue(json, DatosReciboResponse.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo interpretar el JSON de los datos del recibo.");
        }
    }

    public ApiResponse<String> finalizarOrdenYGenerarRecibo(String ordenUuid, Integer tipoSerieRecibo, String usuario, String correo) {
        FinalizarOrdenCobranzaResponse orden = finalizarOrdenDeCobranza(ordenUuid, tipoSerieRecibo, usuario);
        DatosReciboResponse recibo = datosRecibo(orden.numeroRecibo(), orden.serieReciboId());

        // 3. GENERACIÓN DE PDF Y NOTIFICACIÓN
        try {
            // Generamos el archivo
            UUID url = generador.generarYGuardarRecibo(recibo);

            // Preparamos y enviamos la notificación
            SolicitudNotificacionDto solicitudNotificacion = SolicitudNotificacionDto.builder()
                    .codigoSistema("FACIL")
                    .aliasConfig("SMTP_GENERAL")
                    .destinatarios(List.of(correo))
                    .cuerpo("Gracias por su pago, aquí tiene su recibo.")
                    .prioridad(10)
                    .adjuntos(List.of(url.toString()))
                    .build();

            notificationClient.enviarNotificacion(solicitudNotificacion);

            return ApiResponse.success("Orden finalizada con éxito.", url.toString());

        } catch (Exception e) {
            return ApiResponse.error(GeneralResponseCode.SERVICE_UNAVAILABLE, "El pago se procesó, pero hubo un problema al generar el PDF o enviar el correo. Puede descargarlo desde su historial.");
        }
    }
}
