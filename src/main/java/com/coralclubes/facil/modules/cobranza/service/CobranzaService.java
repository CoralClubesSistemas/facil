package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.projection.DatosReciboResponse;
import com.coralclubes.facil.modules.cobranza.dto.projection.ReciboPagado;
import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.*;
import com.coralclubes.facil.modules.cobranza.engines.pagos.engine.PaymentStrategyFactory;
import com.coralclubes.facil.modules.cobranza.engines.pagos.interfaces.PaymentStrategy;
import com.coralclubes.facil.modules.cobranza.repository.CobranzaRepository;
import com.coralclubes.facil.modules.cobranza.repository.IntentoPagoRepository;
import com.coralclubes.facil.modules.usuarios.service.UsuarioService;
import com.coralclubes.facil.shared.events.dto.ReciboPagadoEvent;
// import com.coralclubes.facil.shared.infrastructure.integration.ia.analisis.AnalisisDeInformacion;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitarUrlRequest;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.coralclubes.facil.modules.cobranza.dto.projection.ContextoFinalizacionOrdenResponse;
import com.coralclubes.facil.modules.cobranza.dto.projection.MovimientoIntencionPersistenciaDto;
import com.coralclubes.facil.modules.cobranza.dto.request.AplicarCierreOrdenPayloadDto;
import com.coralclubes.facil.modules.cobranza.dto.request.SimularCalculoDescuentoRequest;
import com.coralclubes.facil.modules.cobranza.engines.CobranzaCalculoEngine;
import com.coralclubes.facil.modules.cobranza.engines.CobranzaLiquidacionEngine;
import com.coralclubes.facil.modules.cobranza.repository.CobranzaCatalogosRepository;
import java.util.UUID;

import com.coralclubes.facil.modules.cobranza.dto.request.AnalizarComprobanteRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.SolicitarUrlComprobanteRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.AnalizarComprobanteResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.ComprobantePagoAnalizadoDto;
import com.coralclubes.facil.modules.cobranza.service.extractor.ComprobanteDigitalParser;
import com.coralclubes.facil.modules.cobranza.service.extractor.ComprobanteSanitizer;
import com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.client.FilesAnalysisClient;
import com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.dto.AnalisisArchivoSolicitud;
import com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.dto.ResultadoAnalisis;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class CobranzaService {
    private final CobranzaRepository repository;
    private final CobranzaCatalogosRepository catalogosRepository;
    private final CobranzaCalculoEngine calculoEngine;
    private final CobranzaLiquidacionEngine liquidacionEngine;
    private final ObjectMapper objectMapper;
    private final UsuarioService usuarioService;
    private final IntentoPagoRepository intentoPagoRepository;
    private final PaymentStrategyFactory strategyFactory;
    private final UserContext userContext;
    private final CobranzaPostProcesoAsyncService postProcesoAsyncService;
    private final ApplicationEventPublisher eventPublisher;
    private final FilesAnalysisClient filesAnalysisClient;
    private final StorageClient storageClient;
    private final ComprobanteDigitalParser digitalParser;
    private final ComprobanteSanitizer comprobanteSanitizer;

    @Value("classpath:prompts/comprobante-pago-prompt.md")
    private org.springframework.core.io.Resource promptComprobanteResource;

    @Value("${app.email.audit-default}")
    private String emailAuditDefault;

    public ApiResponse<SimularCalculoDescuentoResponse> simularCalculoDescuento(SimularCalculoDescuentoRequest request) {
        BigDecimal porcentajeAutorizado = null;
        if (request.idDesarrollo() != null && request.clasificacionMembresia() != null) {
            porcentajeAutorizado = catalogosRepository.spCobranzaObtenerPorcentajeLimite(
                    request.idDesarrollo(), request.clasificacionMembresia());
        }

        SimularCalculoDescuentoResponse response = calculoEngine.simularCalculo(request, porcentajeAutorizado);
        return ApiResponse.success("Cálculo de descuentos simulado correctamente.", response);
    }

    public ApiResponse<GenerarOrdenCobranzaResponse> generarOrdenCobranza(GenerarOrdenCobranzaRequest request, String usuario) {
        BigDecimal porcentajeAutorizado = null;
        if (request.idDesarrollo() != null && request.clasificacionMembresia() != null) {
            porcentajeAutorizado = catalogosRepository.spCobranzaObtenerPorcentajeLimite(
                    request.idDesarrollo(), request.clasificacionMembresia());
        }

        List<MovimientoIntencionPersistenciaDto> intenciones = calculoEngine.procesarParaPersistencia(
                request, porcentajeAutorizado);

        String movimientosJson = serializarIntenciones(intenciones);

        log.info("[{}] Generando orden de cobranza para membresía {} con intenciones procesadas: {}", usuario, request.membresia(), movimientosJson);

        GenerarOrdenCobranzaResponse result = repository
                .spCobranzaGenerarOrdenCobranza(
                        request.membresia(),
                        usuario,
                        movimientosJson,
                        Boolean.TRUE.equals(request.ivaIncluido()),
                        request.mensajeAdicional()
                )
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

    public ReciboPagado finalizarOrdenDeCobranza(String ordenUuid, Integer tipoSerieRecibo, String usuario) {
        // 1. Obtener contexto completo desde la BD
        String contextoJson = repository.spCobranzaObtenerContextoFinalizacionOrden(ordenUuid, tipoSerieRecibo, usuario)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró contexto para la orden de cobranza a finalizar."));

        ContextoFinalizacionOrdenResponse contexto;
        try {
            contexto = objectMapper.readValue(contextoJson, ContextoFinalizacionOrdenResponse.class);
        } catch (JsonProcessingException ex) {
            log.error("[{}] Error deserializando contexto de orden {}: {}", usuario, ordenUuid, ex.getMessage());
            throw new IllegalStateException("No se pudo interpretar el contexto de la orden de cobranza.");
        }

        // 2. Ejecutar lógica contable y corrección de saldos en Java
        AplicarCierreOrdenPayloadDto payload = liquidacionEngine.armarPlanDeLiquidacion(contexto, tipoSerieRecibo, usuario);

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el plan de liquidación contable.");
        }

        log.info("[{}] Aplicando cierre contable para orden {} con importe {}: {}",
                usuario, ordenUuid, payload.importeRecibo(), payloadJson);

        // 3. Persistir atómicamente en SQL Server
        String response = repository.spCobranzaAplicarCierreOrdenYRecibo(ordenUuid, usuario, payloadJson)
                .orElseThrow(() -> new IllegalArgumentException("Error al aplicar el cierre de la orden de cobranza."));

        try {
            return objectMapper.readValue(response, ReciboPagado.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo interpretar el resultado de la orden finalizada.");
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
        String correoAuditoria = usuarioService.obtenerCorreoUsuario(usuario).orElse(emailAuditDefault);

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
                    log.error("[{}] Error en post-procesamiento de forma de pago ID {}: {}", usuario, intento.intentoPagoId(), e.getMessage());
                }
            }
        }
        // =================================================================================

        // 3. Obtener datos procesados para los PDFs
        // SOLO SI EL ESTATUS DEL RECIBO ES 'PAGADO'
        if (r.estatusRecibo().equalsIgnoreCase("PAGADO")) {
            log.info("[{}] Recibo {}-{} para membresía {} finalizado con estatus PAGADO. Iniciando generación de documentos y notificaciones.", usuario, orden.serieReciboId(), orden.numeroRecibo(), orden.membresia());

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
            log.info("[{}] Recibo {}-{} para membresía {} finalizado con estatus {}. No se generarán documentos ni notificaciones.", usuario, orden.serieReciboId(), orden.numeroRecibo(), orden.membresia(), r.estatusRecibo());
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

        log.info("[{}] Orden de cobranza finalizada y evento de recibo pagado publicado para membresía: {}, recibo: {}-{}", usuario, orden.membresia(), orden.serieReciboId(), orden.numeroRecibo());

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

    public Optional<String> obtenerSiguienteMembresiaPendiente(String membresiaActual) {
        String usuario = userContext.getUsername();
        return repository.spCobranzaObtenerSiguienteMembresiaPendiente(usuario, membresiaActual);
    }

    /**
     * Solicita una URL prefirmada a Coral Almacenamiento para subir un comprobante de pago (Valet Key).
     */
    public RespuestaCargaDto solicitarUrlCargaComprobante(SolicitarUrlRequest request, String usuario) {
        java.util.Map<String, String> metadatos = java.util.Map.of(
                "tipoProceso", "COMPROBANTE_DEPOSITO",
                "subidoPor", usuario != null ? usuario : "SYSTEM"
        );

        SolicitudCargaDto solicitud = SolicitudCargaDto.builder()
                .nombreArchivo(request.nombreArchivo())
                .contentType(request.contentType())
                .tamanoBytes(request.tamanoBytes())
                .esPublico(false)
                .rutaLogica("cobranza/comprobantes")
                .metadatos(metadatos)
                .build();
        return storageClient.solicitarUrlCarga(solicitud);
    }

    /**
     * Solicita el análisis inteligente del comprobante bancario previamente cargado en Coral Almacenamiento.
     * Utiliza extracción digital directa si es PDF con texto estructurado, o visión multimodal de Bedrock en fallback/imágenes.
     * Soporta especificar opcionalmente el número de página a analizar en PDFs multipágina.
     */
    public DepositoCobranzaDto buscarDepositosPorComprobante(AnalizarComprobanteRequest request, String usuario) {
        UUID fileId = request.fileId();
        Integer numeroPagina = request.numeroPagina() != null ? request.numeroPagina() : 1;

        log.info("{} solicita análisis de comprobante de depósito con fileId {} (página {})", usuario, fileId, numeroPagina);

        // ====== procesamos el archivo mediante la integracion de files analysis ======
        String promptInstrucciones = cargarPromptComprobante();

        AnalisisArchivoSolicitud<ComprobantePagoAnalizadoDto> solicitud = new AnalisisArchivoSolicitud<>(
                fileId,
                numeroPagina,
                ComprobantePagoAnalizadoDto.class,
                promptInstrucciones,
                digitalParser::parsear
        );

        ResultadoAnalisis<ComprobantePagoAnalizadoDto> resultado = filesAnalysisClient.analizar(solicitud);

        // Del resultado obtenido, sanitizamos los datos (eliminamos caracteres extraños, normalizamos mayúsculas/minúsculas, reemplazamos campos, etc.)
        ComprobantePagoAnalizadoDto datosSanitizados = comprobanteSanitizer.sanitizar(resultado.datos());

        System.out.println("Datos sanitizados del comprobante: " + datosSanitizados);

        return null;
    }

    // *************** HELPERS ******************************
    private String serializarIntenciones(List<MovimientoIntencionPersistenciaDto> intenciones) {
        try {
            return objectMapper.writeValueAsString(intenciones);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el detalle de intenciones calculadas para la orden.");
        }
    }

    private String cargarPromptComprobante() {
        try {
            if (promptComprobanteResource != null && promptComprobanteResource.exists()) {
                return promptComprobanteResource.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("Error al leer la plantilla de prompt externa para comprobantes: {}", e.getMessage());
        }
        return """
                Analiza el comprobante bancario adjunto.
                Extrae con la máxima precisión financiera los siguientes campos:
                - bancoEmisor, bancoReceptor, monto, fechaOperacion, horaOperacion, claveRastreo, referencia,
                  cuentaOrdenante, cuentaBeneficiaria, beneficiario, ordenante, concepto, tipoOperacion.
                """;
    }
}
