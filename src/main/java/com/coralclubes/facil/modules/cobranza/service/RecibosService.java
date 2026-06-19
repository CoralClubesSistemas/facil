package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.projection.DatosReciboResponse;
import com.coralclubes.facil.modules.cobranza.dto.request.CancelarReciboRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.RegistarEvidenciaReciboCancelado;
import com.coralclubes.facil.modules.cobranza.dto.response.*;
import com.coralclubes.facil.modules.cobranza.repository.CobranzaRepository;
import com.coralclubes.facil.modules.cobranza.repository.RecibosRepository;
import com.coralclubes.facil.shared.events.dto.ReciboCanceladoEvent;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitarUrlRequest;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaDto;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.facil.shared.domain.dto.ArchivoDescarga;
import java.util.UUID;
import java.util.Optional;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecibosService {

    private final RecibosRepository recibosRepository;
    private final CobranzaRepository cobranzaRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final UserContext userContext;
    private final BusinessLogger businessLogger;
    private final CobranzaPostProcesoAsyncService postProceso;
    private final CobranzaGeneradorDocumentosService generador;

    private final StorageClient storageClient; 

    @Value("${app.clients.storage.aliases.default}")
    private String aliasStorageDefault;

    /**
     * Busca recibos de cobranza con múltiples filtros opcionales.
     *
     * @param folioRecibo        Formato: numero-serieDescripcion
     * @param fechaGeneracionDe  Fecha desde (ISO 8601)
     * @param fechaGeneracionA   Fecha hasta (ISO 8601)
     * @param membresia          Identificador de membresía
     * @param desarrolloId       ID del desarrollo
     * @param usuario            Código de usuario que generó el recibo
     * @param nombreSocio        Búsqueda en nombre completo del cliente
     * @param terminacionTarjeta Últimos dígitos de tarjeta (si aplica)
     * @param filtrarPorEstatus  1 = solo Generado (684), 0 = múltiples estatus
     * @return Respuesta con lista de recibos encontrados
     */
    public ApiResponse<List<BuscarRecibosResponse>> buscarRecibos(
            String folioRecibo,
            LocalDate fechaGeneracionDe,
            LocalDate fechaGeneracionA,
            String membresia,
            Integer desarrolloId,
            String usuario,
            String nombreSocio,
            String terminacionTarjeta,
            Boolean filtrarPorEstatus
    ) {
        var usuarioFinal = usuario;

        // si la bandera de filtrar por estatus viene activa es por que se esta ahciendo la consulta desde el
        // modulo de cancelacion de recibos no pagados y en este modulo solo podemos obtener los recibos generados por el usuario que consulta
        if (filtrarPorEstatus) {
            usuarioFinal = userContext.getUsername();
        }

        List<BuscarRecibosResponse> resultados = recibosRepository.spCobranzaBuscarRecibos(
                folioRecibo,
                fechaGeneracionDe,
                fechaGeneracionA,
                membresia,
                desarrolloId,
                usuarioFinal,
                nombreSocio,
                terminacionTarjeta,
                filtrarPorEstatus
        );
        return ApiResponse.success("Recibos encontrados correctamente.", resultados);
    }

    public ApiResponse<ObtenerDetallesReciboResponse> obtenerDetallesRecibo(
            Integer numeroRecibo,
            Integer serieReciboId,
            String membresia
    ) {
        String detallesJson = recibosRepository
                .spCobranzaObtenerDetallesRecibo(numeroRecibo, serieReciboId, membresia)
                .orElseThrow(() -> new IllegalStateException("No se encontraron detalles para el recibo solicitado."));

        if (detallesJson.isBlank()) {
            throw new IllegalStateException("La consulta de detalles de recibo regresó un JSON vacío.");
        }

        try {
            ObtenerDetallesReciboResponse response = objectMapper.readValue(detallesJson, ObtenerDetallesReciboResponse.class);
            return ApiResponse.success("Detalles de recibo obtenidos correctamente.", response);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo interpretar el JSON de detalles del recibo.");
        }
    }

    public ApiResponse<Boolean> registrarEvidenciaReciboCancelado(RegistarEvidenciaReciboCancelado request) {
        String usuario = userContext.getUsername();

        try {
            String jsonFiles = objectMapper.writeValueAsString(request.jsonFiles());

            recibosRepository.spCobranzaRegistarEvidenciaReciboCancelado(
                    request.numeroMembresia(),
                    request.numeroRecibo(),
                    request.idSerieRecibo(),
                    usuario,
                    jsonFiles
            );

            businessLogger.info(usuario,
                    "Evidencia de recibo cancelado registrada, membresia: {}, recibo: {}, serie: {}, archivos: {}",
                    request.numeroMembresia(), request.numeroRecibo(), request.idSerieRecibo(), request.jsonFiles().size());

            return ApiResponse.success("Evidencia registrada correctamente.", true);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar la lista de archivos de evidencia.");
        }
    }

    @Transactional
    public ApiResponse<Boolean> cancelarRecibo(CancelarReciboRequest request) {
        String usuario = userContext.getUsername();
        ReciboCanceladoEvent evento;
        DatosReciboResponse datosRecibo;

        String reciboJson = cobranzaRepository.spCobranzaObtenerDatosRecibo(
                request.numeroRecibo(),
                request.serieReciboId(),
                request.membresia()
        ).orElseThrow(() -> new IllegalStateException("No se encontraron datos para el recibo solicitado."));

        try {
            datosRecibo = objectMapper.readValue(reciboJson, DatosReciboResponse.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo interpretar el JSON de datos del recibo.");
        }

        // 1. Ejecutar rollback contable en BD
        String response = recibosRepository.spCobranzaCancelarRecibo(
                request.membresia(),
                request.numeroRecibo(),
                request.serieReciboId(),
                usuario,
                request.razonCancelacion()
        ).orElseThrow(() -> new IllegalStateException("Error al cancelar el recibo, intente más tarde."));

        // parseamos el json
        try {
            evento = objectMapper.readValue(response, ReciboCanceladoEvent.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo interpretar el JSON de respuesta de cancelación del recibo.");
        }

        businessLogger.info(usuario, "Evento de cancelacion de recibo publicado, membresia: {}, recibo: {}, serie: {}",
                request.membresia(), request.numeroRecibo(), request.serieReciboId());

        ReciboCanceladoEvent eventoFinal = new ReciboCanceladoEvent(
                evento.membresia(),
                evento.tipoMembresia(),
                evento.clasificacionMembresia(),
                evento.usuario(),
                evento.motivoCancelacion(),
                evento.correoCliente(),
                evento.correoUsuario(),
                evento.movimientosAfectados(),
                request.decisionesUsuario()
        );

        // 2. Disparar el evento de dominio (Orquestación desacoplada)
        // El resto de los módulos (Reservas, Puntos) estarán escuchando este record
        eventPublisher.publishEvent(eventoFinal);

        // Procesamos de forma asincrona la generacion y envio del pdf de cancelacion
        postProceso.procesarReciboCanceladoYNotificar(datosRecibo, usuario, evento.correoCliente(), evento.correoUsuario());

        return ApiResponse.success("Recibo cancelado exitosamente.", true);
    }

    public ApiResponse<List<RespuestaCargaDto>> solicitarUrlsDeCarga(List<SolicitarUrlRequest> solicitudes) {
        String usuario = userContext.getUsername();

        // 2. Construir la ruta lógica inmutable
        String rutaLogica = "cobranza/evidencia-cancelacion-recibos/";

        List<RespuestaCargaDto> respuestas = solicitudes.stream()
                .map(solicitud -> {
                    String ruta = rutaLogica + solicitud.id();

                    SolicitudCargaDto solicitudStorage = SolicitudCargaDto.builder()
                            .idCorrelacion(String.valueOf(solicitud.id()))
                            .requiereDepuracion(true)
                            .nombreArchivo(solicitud.nombreArchivo())
                            .contentType(solicitud.contentType())
                            .tamanoBytes(solicitud.tamanoBytes())
                            .aliasConfiguracion(aliasStorageDefault)
                            .esPublico(false)
                            .rutaLogica(ruta)
                            .metadatos(Map.of(
                                    "modulo", "COBRANZA",
                                    "recibo", String.valueOf(solicitud.id()),
                                    "subidoPor", usuario
                            ))
                            .build();

                    return storageClient.solicitarUrlCarga(solicitudStorage);
                })
                .toList();

        return ApiResponse.success("URLs de carga solicitadas exitosamente.", respuestas);
    }

    public ReciboDigitalDto obtenerReciboDigital(
            String membresia,
            Integer numeroRecibo,
            Integer idSerieRecibo
    ) {
        String usuario = userContext.getUsername();

        // 1. Intentar obtener el recibo digital ya registrado en la BD
        Optional<ReciboDigitalDto> reciboOpt = recibosRepository
                .spCobranzaObtenerReciboDigital(membresia, numeroRecibo, idSerieRecibo);

        if (reciboOpt.isPresent() && reciboOpt.get().activo() != null) {
            return reciboOpt.get();
        }

        // 2. Si no se encuentra registrado o no tiene archivo activo:
        // Consultar detalles del recibo
        String detallesJson = recibosRepository
                .spCobranzaObtenerDetallesRecibo(numeroRecibo, idSerieRecibo, membresia)
                .orElseThrow(() -> new IllegalArgumentException("No se encontraron detalles para el recibo solicitado."));

        ObtenerDetallesReciboResponse detalles;
        try {
            detalles = objectMapper.readValue(detallesJson, ObtenerDetallesReciboResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo interpretar el JSON de detalles del recibo.", e);
        }

        String estatus = detalles.estatus();
        if (estatus == null) {
            throw new IllegalStateException("El estatus del recibo no está definido.");
        }

        String estatusNorm = estatus.trim().toUpperCase();

        // 3. Obtener datos del recibo para el PDF
        String datosJson = cobranzaRepository
                .spCobranzaObtenerDatosRecibo(numeroRecibo, idSerieRecibo, membresia)
                .orElseThrow(() -> new IllegalArgumentException("No se encontraron datos para la generación del PDF del recibo."));

        DatosReciboResponse datosRecibo;
        try {
            datosRecibo = objectMapper.readValue(datosJson, DatosReciboResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo interpretar el JSON de datos del recibo para el PDF.", e);
        }

        if (estatusNorm.equals("CANCELADO") || estatusNorm.equals("CANCELADO SIN PAGO")) {
            // Generar 3 documentos
            UUID original = generador.generarYCargarPdfRecibo(datosRecibo, "ORIGINAL", postProceso.generarCadenaSeguridad(datosRecibo, "ORIGINAL"));
            UUID reimpresion = generador.generarYCargarPdfRecibo(datosRecibo, "REIMPRESION", postProceso.generarCadenaSeguridad(datosRecibo, "REIMPRESION"));
            UUID cancelado = generador.generarYCargarPdfRecibo(datosRecibo, "CANCELACION", postProceso.generarCadenaSeguridad(datosRecibo, "CANCELACION"));

            // Actualizar metadatos digitales para Original y Reimpresión
            recibosRepository.spCobranzaActualizarMetadatosDigitales(
                    membresia,
                    numeroRecibo,
                    idSerieRecibo,
                    String.valueOf(original),
                    String.valueOf(reimpresion),
                    postProceso.generarCadenaSeguridad(datosRecibo, "ORIGINAL"),
                    usuario
            );

            // Actualizar metadatos digitales para Cancelación
            recibosRepository.spCobranzaActualizarCancelacionReciboDigital(
                    membresia,
                    numeroRecibo,
                    idSerieRecibo,
                    String.valueOf(cancelado)
            );

            businessLogger.info(usuario, "Generación y registro digital de los 3 recibos (Original, Reimpresión, Cancelación) completado para folio: " + datosRecibo.getFolio() + ", estatus: " + estatus);

        } else {
            // Generar solo 2 documentos (Original y Reimpresión)
            UUID original = generador.generarYCargarPdfRecibo(datosRecibo, "ORIGINAL", postProceso.generarCadenaSeguridad(datosRecibo, "ORIGINAL"));
            UUID reimpresion = generador.generarYCargarPdfRecibo(datosRecibo, "REIMPRESION", postProceso.generarCadenaSeguridad(datosRecibo, "REIMPRESION"));

            // Actualizar metadatos digitales para Original y Reimpresión
            recibosRepository.spCobranzaActualizarMetadatosDigitales(
                    membresia,
                    numeroRecibo,
                    idSerieRecibo,
                    String.valueOf(original),
                    String.valueOf(reimpresion),
                    postProceso.generarCadenaSeguridad(datosRecibo, "ORIGINAL"),
                    usuario
            );

            businessLogger.info(usuario, "Generación y registro digital de los 2 recibos (Original, Reimpresión) completado para folio: " + datosRecibo.getFolio() + ", estatus: " + estatus);
        }

        return recibosRepository.spCobranzaObtenerReciboDigital(membresia, numeroRecibo, idSerieRecibo)
                .orElseThrow(() -> new IllegalStateException("No se pudo obtener el recibo digital después de su generación."));
    }

    public ApiResponse<ArchivoDescarga> obtenerUrlDescargaReciboDigital(
            String membresia,
            Integer numeroRecibo,
            Integer idSerieRecibo
    ) {
        UUID uuidActivo = obtenerReciboDigital(membresia, numeroRecibo, idSerieRecibo).activo();
        ArchivoDescarga archivo = storageClient.obtenerUrlDescargaYNombre(uuidActivo, "inline");
        return ApiResponse.success("URL de descarga del recibo digital obtenida correctamente.", archivo);
    }
}
