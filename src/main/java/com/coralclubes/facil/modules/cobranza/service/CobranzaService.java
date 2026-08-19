package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.projection.DatosReciboResponse;
import com.coralclubes.facil.modules.cobranza.dto.projection.ReciboPagado;
import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.*;
import com.coralclubes.facil.modules.cobranza.model.pagos.engine.PaymentStrategyFactory;
import com.coralclubes.facil.modules.cobranza.model.pagos.interfaces.PaymentStrategy;
import com.coralclubes.facil.modules.cobranza.repository.CobranzaRepository;
import com.coralclubes.facil.modules.cobranza.repository.IntentoPagoRepository;
import com.coralclubes.facil.modules.usuarios.service.UsuarioService;
import com.coralclubes.facil.shared.events.dto.ReciboPagadoEvent;
// import com.coralclubes.facil.shared.infrastructure.integration.ia.analisis.AnalisisDeInformacion;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.NotificationClient;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CobranzaService {
    private final CobranzaRepository repository;
    private final ObjectMapper objectMapper;
    private final BusinessLogger log;
    private final UsuarioService usuarioService;
    private final IntentoPagoRepository intentoPagoRepository;
    private final PaymentStrategyFactory strategyFactory;
    private final UserContext userContext;
    private final CobranzaPostProcesoAsyncService postProcesoAsyncService;
    private final ApplicationEventPublisher eventPublisher;

    // private final AnalisisDeInformacion bedrockClient;

    public ApiResponse<GenerarOrdenCobranzaResponse> generarOrdenCobranza(GenerarOrdenCobranzaRequest request, String usuario) {
        String movimientosJson = serializarMovimientos(request);

        GenerarOrdenCobranzaResponse result = repository
                .spCobranzaGenerarOrdenCobranza(request.membresia(), usuario, movimientosJson, request.agregarIva(), request.ivaIncluido(), request.mensajeAdicional())
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

    public ApiResponse<List<DepositoCobranzaDto>> obtenerDepositos(Integer idBanco, LocalDate fechaDeposito, String busqueda, BigDecimal monto) {
        return ApiResponse.success(
                "Depositos obtenidos correctamente.",
                repository.spCobranzaObtenerDepositos(idBanco, fechaDeposito, busqueda, monto)
        );
    }

    private String serializarMovimientos(GenerarOrdenCobranzaRequest request) {
        try {
            return objectMapper.writeValueAsString(request.movimientos());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el detalle de movimientos para la orden.");
        }
    }

    public ReciboPagado finalizarOrdenDeCobranza(String ordenUuid, Integer tipoSerieRecibo, String usuario) {
        String response = repository.spCobranzaFinalizarOrdenYGenerarRecibo(ordenUuid, tipoSerieRecibo, usuario)
                .orElseThrow(() -> new IllegalArgumentException("Error en el cierre de la orden de cobranza, intente más tarde"));

        try {
            return objectMapper.readValue(response, ReciboPagado.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo interpretar el JSON de la orden finalizada.");
        }
    }

    public DatosReciboResponse datosRecibo(Integer numeroRecibo, Integer serieReciboId, String membresia) {
        String json = repository.spCobranzaObtenerDatosRecibo(numeroRecibo, serieReciboId, membresia)
                .orElseThrow(() -> new IllegalArgumentException("No se encontraron datos para el recibo solicitado."));

        try {
            return objectMapper.readValue(json, DatosReciboResponse.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo interpretar el JSON de los datos del recibo.");
        }
    }

    @Transactional
    public ApiResponse<FinalizarOrdenCobranzaResponse> finalizarOrdenYGenerarRecibo(
            String ordenUuid,
            Integer tipoSerieRecibo,
            String usuario,
            List<String> correos
    ) {
        String correoAuditoria = usuarioService.obtenerCorreoUsuario(usuario).orElse("facil@coralclubes.com");

        // 1. Ejecutar transacción CORE en SQL (Genera Recibo y Movimientos)
        ReciboPagado r = finalizarOrdenDeCobranza(ordenUuid, tipoSerieRecibo, usuario);
        FinalizarOrdenCobranzaResponse orden = new FinalizarOrdenCobranzaResponse(r.numeroRecibo(), r.serieReciboId(), r.membresia(), r.totalPagado());

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
        // SOLO SI EL ESTATUS DEL RECIBO ES 'PAGADO'
        if (r.estatusRecibo().equalsIgnoreCase("PAGADO")) {
            log.info(usuario, "Recibo {}-{} para membresía {} finalizado con estatus PAGADO. Iniciando generación de documentos y notificaciones.", orden.serieReciboId(), orden.numeroRecibo(), orden.membresia());

            DatosReciboResponse recibo = datosRecibo(orden.numeroRecibo(), orden.serieReciboId(), orden.membresia());

            // 4. Delegar la generación del PDF, metadatos y correos al hilo en SEGUNDO PLANO
            postProcesoAsyncService.procesarDocumentosYNotificaciones(
                    orden,
                    recibo,
                    usuario,
                    correos,
                    correoAuditoria
            );
        } else {
            log.info(usuario, "Recibo {}-{} para membresía {} finalizado con estatus {}. No se generarán documentos ni notificaciones.", orden.serieReciboId(), orden.numeroRecibo(), orden.membresia(), r.estatusRecibo());
        }

        // publicacion de evento de recibo pagado
        ReciboPagadoEvent reciboPagadoEvent = ReciboPagadoEvent.builder()
                .ordenUuid(ordenUuid)
                .membresia(r.membresia())
                .numeroRecibo(r.numeroRecibo())
                .serieReciboId(r.serieReciboId())
                .tipoMembresia(r.tipoMembresia())
                .clasificacionMembresia(r.clasificacionMembresia())
                .usuario(r.usuario())
                .desarrolloId(r.desarrolloId())
                .totalPagado(r.totalPagado())
                .movimientosAfectados(
                        r.movimientosAfectados().stream()
                                .map(m -> new ReciboPagadoEvent.MovimientosReciboPagado(
                                        m.idMovimiento(),
                                        m.tipoMovimiento(),
                                        m.montoPagado(),
                                        m.estatusId(),
                                        m.estatus()
                                ))
                                .toList()
                )
                .build();

        eventPublisher.publishEvent(reciboPagadoEvent);

        log.info(usuario, "Orden de cobranza finalizada y evento de recibo pagado publicado para membresía: {}, recibo: {}-{}", orden.membresia(), orden.serieReciboId(), orden.numeroRecibo());

        return ApiResponse.success("El cobro se procesó correctamente. Los recibos se están generando y enviando en segundo plano.", orden);
    }

    public void cancelarOrdenCobranzaSinPago(String uuid) {
        String usuario = userContext.getUsername();
        repository.spCobranzaCancelarOrdenCobranzaSinPago(uuid, usuario);
    }

    public ApiResponse<List<RecibosCancelados>> obtenerRecibosCancelados(String membresia, String recibo) {
        return ApiResponse.success("Recibos obtenidos correctamente", repository.spCobranzaObtenerRecibosCancelados(membresia, recibo));
    }

    public ApiResponse<List<CarteraEjecutivoResponse>> obtenerCarteraEjecutivo() {
        String usuario = userContext.getUsername();
        return ApiResponse.success(
                "Cartera de ejecutivo obtenida correctamente.",
                repository.spCobranzaObtenerCarteraEjecutivo(usuario)
        );
    }


    /* public ApiResponse<AnalisisCobranzaResponse> analizarClienteParaCobranza(String membresia) {
        String dataJsonCliente = repository.spClientesObtenerDataParaAnalisis(membresia)
                .orElseThrow(() -> new IllegalStateException("No se encontró información para la membresía proporcionada."));

        // 2. Definir el System Prompt específico para este caso de uso
        String systemPrompt = """
                Eres un experto analista financiero. Analiza el JSON del cliente.
                Debes devolver la respuesta ESTRICTAMENTE en formato JSON con la siguiente estructura, sin formato Markdown ni texto antes o después:
                {
                  "clasificacionRiesgo": "Excelente | Regular | Moroso | Riesgo de Abandono",
                  "justificacionAnalisis": "Breve explicación del por qué de la clasificación basada en sus pagos y notas",
                  "mensajeWhatsappRecomendado": "El mensaje de cobranza persuasivo y empático listo para enviar"
                }
                """;

        // 3. Solicitar el análisis a Bedrock
        String respuestaIa = bedrockClient.analizarData(systemPrompt, dataJsonCliente);

        // 4. Mapear el JSON de respuesta devuelto por la IA a nuestro Record de Java
        try {
            AnalisisCobranzaResponse analisis = objectMapper.readValue(respuestaIa, AnalisisCobranzaResponse.class);
            return ApiResponse.success("Análisis de IA generado correctamente.", analisis);
        } catch (JsonProcessingException e) {
            log.error(userContext.getUsername(), "La IA no devolvió un JSON válido: {}", respuestaIa);
            throw new IllegalStateException("Ocurrió un error al procesar la respuesta de la inteligencia artificial.");
        }
    }*/

    public Optional<String> obtenerSiguienteMembresiaPendiente(String membresiaActual) {
        String usuario = userContext.getUsername();
        return repository.spCobranzaObtenerSiguienteMembresiaPendiente(usuario, membresiaActual);
    }
}
