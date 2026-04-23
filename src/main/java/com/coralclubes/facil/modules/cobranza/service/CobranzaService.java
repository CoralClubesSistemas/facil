package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.projection.DatosReciboResponse;
import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.*;
import com.coralclubes.facil.modules.cobranza.model.pagos.engine.PaymentStrategyFactory;
import com.coralclubes.facil.modules.cobranza.model.pagos.interfaces.PaymentStrategy;
import com.coralclubes.facil.modules.cobranza.repository.CobranzaRepository;
import com.coralclubes.facil.modules.cobranza.repository.IntentoPagoRepository;
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

import java.time.LocalDate;
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

    private final IntentoPagoRepository intentoPagoRepository;
    private final PaymentStrategyFactory strategyFactory;

    private final CobranzaPostProcesoAsyncService postProcesoAsyncService;

    public ApiResponse<GenerarOrdenCobranzaResponse> generarOrdenCobranza(GenerarOrdenCobranzaRequest request, String usuario) {
        String movimientosJson = serializarMovimientos(request);

        GenerarOrdenCobranzaResponse result = repository
                .spCobranzaGenerarOrdenCobranza(request.membresia(), usuario, movimientosJson, request.agregarIva(), request.ivaIncluido())
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

    public ApiResponse<RecuperarOrdenCobranzaResponse> recuperarOrdenCobranza(Integer movimientoId, String membresia) {
        UUID ordenUuid = repository
                .spCobranzaRecuperarOrdenCobranza(movimientoId, membresia)
                .orElseThrow(() -> new IllegalStateException("No se encontró una orden de cobranza para el movimiento y membresía proporcionados."));

        return ApiResponse.success(
                "Orden de cobranza recuperada correctamente.",
                new RecuperarOrdenCobranzaResponse(ordenUuid)
        );
    }

    public ApiResponse<List<FormaPagoDto>> obtenerFormasDePago() {
        return ApiResponse.success("Formas de pago obtenidas correctamente.", repository.spCobranzaCatalogoFormasDePago());
    }

    public ApiResponse<List<DepositoCobranzaDto>> obtenerDepositos(Integer idBanco, LocalDate fechaDeposito, String busqueda) {
        return ApiResponse.success(
                "Depositos obtenidos correctamente.",
                repository.spCobranzaObtenerDepositos(idBanco, fechaDeposito, busqueda)
        );
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
    public ApiResponse<FinalizarOrdenCobranzaResponse> finalizarOrdenYGenerarRecibo(
            String ordenUuid,
            Integer tipoSerieRecibo,
            String usuario,
            List<String> correos
    ) {
        String correoAuditoria = usuarioService.obtenerCorreoUsuario(usuario).orElse("facil@coralclubes.com");

        try {
            // 1. Ejecutar transacción CORE en SQL (Genera Recibo y Movimientos)
            FinalizarOrdenCobranzaResponse orden = finalizarOrdenDeCobranza(ordenUuid, tipoSerieRecibo, usuario);

            // =================================================================================
            // 2. FASE DE POST-PROCESAMIENTO POR ESTRATEGIA (FORMAS DE PAGO)
            // =================================================================================
            List<IntentoPagoDto> intentos = intentoPagoRepository.spCobranzaObtenerIntentosPagoPorOrden(UUID.fromString(ordenUuid));

            for (IntentoPagoDto intento : intentos) {
                // Solo procesamos los que fueron exitosos
                if ("APROBADO".equalsIgnoreCase(intento.estatus())) {
                    try {
                        PaymentStrategy strategy = strategyFactory.getStrategy(intento.formaPagoClave());
                        strategy.postProcesarFinalizacion(intento.intentoPagoId());
                    } catch (Exception e) {
                        log.error(usuario, "Error en post-procesamiento de forma de pago ID {}: {}", intento.intentoPagoId(), e.getMessage());
                    }
                }
            }
            // =================================================================================

            // 3. Obtener datos procesados para los PDFs
            DatosReciboResponse recibo = datosRecibo(orden.numeroRecibo(), orden.serieReciboId());

            // 4. Delegar la generación del PDF, metadatos y correos al hilo en SEGUNDO PLANO
            postProcesoAsyncService.procesarDocumentosYNotificaciones(
                    orden,
                    recibo,
                    usuario,
                    correos,
                    correoAuditoria
            );

            return ApiResponse.success("El cobro se procesó correctamente. Los recibos se están generando y enviando en segundo plano.", orden);
        } catch (Exception e) {
            log.error(usuario, "Error general finalizando la orden: {}", e.getMessage());
            return ApiResponse.error(GeneralResponseCode.SERVICE_UNAVAILABLE, "Ocurrió un error al procesar el pago o generar los documentos.");
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

    public ApiResponse<List<RecibosCancelados>> obtenerRecibosCancelados(String membresia, String recibo) {
        return ApiResponse.success("Recibos obtenidos correctamente", repository.spCobranzaObtenerRecibosCancelados(membresia, recibo));
    }
}
