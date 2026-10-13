package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.facil.modules.clientes.service.SociosService;
import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocio;
import com.coralclubes.facil.modules.reservaciones.dto.request.DatosCartaOcupacionDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.DetalleReservacionDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.ResultadoQrReservacion;
import com.coralclubes.facil.modules.reservaciones.dto.response.ResumenReservacionDto;
import com.coralclubes.facil.modules.reservaciones.repository.ReservacionesDocumentosRepository;
import com.coralclubes.facil.modules.reservaciones.repository.ReservacionesRepository;
import com.coralclubes.facil.shared.domain.dto.ArchivoDescarga;
import com.coralclubes.facil.shared.domain.enums.ClavesModulos;
import com.coralclubes.facil.shared.domain.enums.DesarrolloLogoEnum;
import com.coralclubes.facil.shared.events.dto.ReservacionConfirmadaEvent;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.NotificationClient;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.InfoArchivoDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaLegacyDto;
import com.coralclubes.facil.shared.platform.qr.dto.CreateQrRequest;
import com.coralclubes.facil.shared.platform.qr.dto.SystemQrDataUriResponse;
import com.coralclubes.facil.shared.platform.qr.dto.SystemQrResponse;
import com.coralclubes.facil.shared.platform.qr.enums.SystemQrStatus;
import com.coralclubes.facil.shared.platform.qr.service.SystemQrService;
import com.coralclubes.facil.shared.platform.templating.service.PdfGeneratorService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservacionesDocumentosService {

    private final ReservacionesDocumentosRepository documentosRepo;
    private final ReservacionesRepository reservacionesRepo;
    private final StorageClient storageClient;
    private final PdfGeneratorService pdfGeneratorService;
    private final SystemQrService systemQrService;
    private final NotificationClient notificationClient;
    private final SociosService sociosService;

    @Value("${app.clients.notifications.templates.reserva-creada}")
    private String templateReservaCreada;

    public ReservacionConfirmadaEvent construirEventDesdeDb(String membresia, Integer consecutivo) {
        DetalleReservacionDto detalle = reservacionesRepo.obtenerDetalleReservacion(membresia, consecutivo);
        ResumenReservacionDto resumen = reservacionesRepo.obtenerResumenReservacion(membresia, consecutivo);

        List<ReservacionConfirmadaEvent.HabitacionInfo> habitaciones = List.of(
                new ReservacionConfirmadaEvent.HabitacionInfo(
                        detalle.tipoUnidad() != null ? detalle.tipoUnidad() : "Habitación Estándar",
                        detalle.numeroSocios() != null && detalle.numeroSocios() > 0 ? detalle.numeroSocios() : 2
                )
        );

        return ReservacionConfirmadaEvent.builder()
                .nombreReserva(detalle.nombreHuesped())
                .email(resumen.emailContacto())
                .email2(null)
                .peticionEspecial(detalle.peticionesEspeciales())
                .membresia(detalle.membresia())
                .fechaEntrada(detalle.fechaEntrada())
                .fechaSalida(detalle.fechaSalida())
                .desarrollo(detalle.nombreDesarrollo())
                .subtotal(detalle.importeTotal())
                .foliosGenerados(List.of(consecutivo))
                .habitaciones(habitaciones)
                .build();
    }

    public ResultadoQrReservacion generarQrReservacion(ReservacionConfirmadaEvent event) {
        String foliosStrJoin = event.foliosGenerados().stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
        String entityId = (event.membresia() != null ? event.membresia() : "GENERAL") + ":" + foliosStrJoin;

        CreateQrRequest qrRequest = CreateQrRequest.builder()
                .module(ClavesModulos.RESERVACIONES.name())
                .submodule(ClavesModulos.RESERVACIONES_RECEPCION.name())
                .actionType("CHECK_IN")
                .entityId(entityId)
                .entityType("RESERVATION")
                .status(SystemQrStatus.ACTIVE)
                .maxUses(1)
                .urlAlmacenamiento("reservaciones/qrs/")
                .metadata(String.format("{\"folios\":%s,\"titular\":\"%s\"}", event.foliosGenerados(), event.nombreReserva()))
                .build();

        SystemQrDataUriResponse qrResponse = systemQrService.crearQrConDataUri(qrRequest);
        return new ResultadoQrReservacion(qrResponse.qrFileId(), qrResponse.dataUri());
    }

    public UUID generarYPersistirCartaOcupacion(ReservacionConfirmadaEvent event, String qrCodeDataUri) {
        List<DatosCartaOcupacionDto.HabitacionCartaDto> habitacionesPdf = new java.util.ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (var hab : event.habitaciones()) {
            habitacionesPdf.add(DatosCartaOcupacionDto.HabitacionCartaDto.builder()
                    .tipoHabitacion(hab.tipoHabitacion())
                    .totalPax(hab.totalPersonas())
                    .build());
        }

        String foliosStr = event.foliosGenerados().toString().replace("[", "").replace("]", "");

        DatosCartaOcupacionDto datosPdf = DatosCartaOcupacionDto.builder()
                .fechaEmision(LocalDate.now().format(formatter))
                .titular(event.nombreReserva())
                .membresia(event.membresia() != null ? event.membresia() : "PÚBLICO GENERAL")
                .foliosReservacion(foliosStr)
                .habitaciones(habitacionesPdf)
                .observaciones(event.peticionEspecial())
                .importeTotal(event.subtotal())
                .fechaEntrada(event.fechaEntrada().format(formatter))
                .fechaSalida(event.fechaSalida().format(formatter))
                .desarrollo(event.desarrollo())
                .build();

        Map<String, Object> variables = new HashMap<>();
        variables.put("fechaEmision", datosPdf.fechaEmision());
        variables.put("titular", datosPdf.titular());
        variables.put("membresia", datosPdf.membresia() != null ? datosPdf.membresia() : "PÚBLICO GENERAL");
        variables.put("foliosReservacion", datosPdf.foliosReservacion());
        variables.put("habitaciones", datosPdf.habitaciones());
        variables.put("observaciones", datosPdf.observaciones() != null ? datosPdf.observaciones() : "Sin observaciones adicionales.");
        java.text.DecimalFormat df = new java.text.DecimalFormat("$#,##0.00");
        variables.put("importeTotal", df.format(datosPdf.importeTotal()));
        variables.put("fechaEntrada", datosPdf.fechaEntrada());
        variables.put("fechaSalida", datosPdf.fechaSalida());
        variables.put("desarrollo", datosPdf.desarrollo());
        variables.put("qrCodeUrl", qrCodeDataUri);

        byte[] pdfBytes = pdfGeneratorService.generarPdfDesdeHtml("CARTA_OCUPACION", variables);

        String foliosLimpio = datosPdf.foliosReservacion().replace(" ", "").replace(",", "_");

        assert datosPdf.membresia() != null;
        String membresiaLimpia = datosPdf.membresia().replace("-", "");

        String nombreArchivo = "CARTA_OCUPACION_" + membresiaLimpia + foliosLimpio + ".pdf";

        SolicitudCargaLegacyDto solicitudCarga = SolicitudCargaLegacyDto.builder()
                .idCorrelacion(event.membresia() + "_" + event.foliosGenerados().getFirst())
                .esPublico(false)
                .rutaLogica("reservaciones/cartas-ocupacion/" + event.membresia())
                .metadatos(Map.of(
                        "folios", event.foliosGenerados().toString(),
                        "subidoPor", "SYSTEM",
                        "modulo", "RESERVACIONES"
                ))
                .requiereDepuracion(false)
                .build();

        InfoArchivoDto info = storageClient.cargarArchivoSincrono(
                pdfBytes,
                nombreArchivo,
                "application/pdf",
                solicitudCarga
        );

        UUID uuidPdf = info.uuid();

        for (Integer consecutivo : event.foliosGenerados()) {
            documentosRepo.spResvGuardarUuidCartaOcupacion(event.membresia(), consecutivo, uuidPdf.toString());
        }

        return uuidPdf;
    }

    public void enviarNotificacionCartaOcupacion(ReservacionConfirmadaEvent event, UUID uuidPdf, UUID qrFileId, List<String> correosAdicionales) {
        List<String> destinatarios = new java.util.ArrayList<>();

        destinatarios.add(event.email());

        if (correosAdicionales != null) {
            for (String correo : correosAdicionales) {
                if (correo != null && !correo.isBlank()) {
                    destinatarios.add(correo);
                }
            }
        }

        if (event.email2() != null && !event.email2().isBlank()) {
            destinatarios.add(event.email2());
        }

        String foliosStr = event.foliosGenerados().toString().replace("[", "").replace("]", "");

        String nombreArchivo = null;
        if (qrFileId != null) {
            try {
                nombreArchivo = storageClient.consultarArchivo(qrFileId).nombreOriginal();
            } catch (Exception e) {
                log.warn("No fue posible consultar la URL de descarga para el QR {}: {}", qrFileId, e.getMessage());
            }
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("nombreUsuario", event.nombreReserva());
        variables.put("numeroReserva", foliosStr);
        if (nombreArchivo != null) {
            variables.put("qrCodeUrl", nombreArchivo);
        }

        List<String> adjuntos = new java.util.ArrayList<>();
        if (uuidPdf != null) {
            adjuntos.add(uuidPdf.toString());
        }
        if (qrFileId != null) {
            adjuntos.add(qrFileId.toString());
        }

        SolicitudNotificacionDto solicitudNotificacion = SolicitudNotificacionDto.builder()
                .destinatarios(destinatarios)
                .codigoPlantilla(templateReservaCreada)
                .variables(variables)
                .prioridad(10)
                .adjuntos(adjuntos)
                .build();

        notificationClient.enviarNotificacion(solicitudNotificacion);
    }

    public ArchivoDescarga obtenerUrlCartaOcupacion(String membresia, Integer consecutivo) {
        Optional<String> uuidStrOpt = documentosRepo.spResvObtenerUuidCartaOcupacion(membresia, consecutivo);
        UUID uuid;
        if (uuidStrOpt.isPresent() && !uuidStrOpt.get().isBlank()) {
            uuid = UUID.fromString(uuidStrOpt.get());
        } else {
            ReservacionConfirmadaEvent event = construirEventDesdeDb(membresia, consecutivo);
            ResultadoQrReservacion qrResultado = obtenerOGenerarQrReservacion(event, consecutivo);
            uuid = generarYPersistirCartaOcupacion(event, qrResultado.qrCodeDataUri());
        }
        return storageClient.obtenerUrlDescarga(uuid, "inline");
    }

    public void reenviarCartaOcupacion(String membresia, Integer consecutivo, String correos) {
        Optional<String> uuidStrOpt = documentosRepo.spResvObtenerUuidCartaOcupacion(membresia, consecutivo);
        ReservacionConfirmadaEvent event = construirEventDesdeDb(membresia, consecutivo);

        List<String> correosAdicionales = correos == null || correos.isBlank()
                ? List.of()
                : java.util.Arrays.stream(correos.split(","))
                .map(String::trim)
                .filter(correo -> !correo.isBlank())
                .toList();

        UUID uuidPdf;
        UUID qrFileId;

        if (uuidStrOpt.isPresent() && !uuidStrOpt.get().isBlank()) {
            uuidPdf = UUID.fromString(uuidStrOpt.get());
            ResultadoQrReservacion qrResultado = obtenerOGenerarQrReservacion(event, consecutivo);
            qrFileId = qrResultado.qrFileId();
        } else {
            ResultadoQrReservacion qrResultado = obtenerOGenerarQrReservacion(event, consecutivo);
            uuidPdf = generarYPersistirCartaOcupacion(event, qrResultado.qrCodeDataUri());
            qrFileId = qrResultado.qrFileId();
        }

        enviarNotificacionCartaOcupacion(event, uuidPdf, qrFileId, correosAdicionales);
    }

    public ResultadoQrReservacion obtenerOGenerarQrReservacion(ReservacionConfirmadaEvent event, Integer consecutivo) {
        String membresiaPrefijo = (event.membresia() != null ? event.membresia() : "GENERAL");
        String exactEntityId = membresiaPrefijo + ":" + consecutivo;
        String pattern = membresiaPrefijo + ":%" + consecutivo + "%";

        Optional<SystemQrResponse> qrOpt = systemQrService.obtenerQrActivoPorModuloYEntidad(
                ClavesModulos.RESERVACIONES.name(),
                "RESERVATION",
                exactEntityId,
                pattern
        );

        if (qrOpt.isPresent()) {
            UUID qrFileId = qrOpt.get().qrFileId();
            String qrDataUri = null;
            if (qrFileId != null) {
                try {
                    qrDataUri = systemQrService.obtenerQrDataUri(qrFileId);
                } catch (Exception e) {
                    log.warn("No fue posible obtener Data URI para QR existente {}: {}", qrFileId, e.getMessage());
                }
            }
            if (qrDataUri != null) {
                return new ResultadoQrReservacion(qrFileId, qrDataUri);
            }
        }

        log.info("No se encontró QR activo válido con archivo para reservación {}:{}. Generando nuevo QR oficial...",
                membresiaPrefijo, consecutivo);
        return generarQrReservacion(event);
    }

    @Transactional
    public byte[] generarPdfRegistroCheckIn(String membresia, Integer numeroReservacion, String usuario) {
        DetalleReservacionDto detalle = reservacionesRepo.obtenerDetalleReservacion(membresia, numeroReservacion);
        if (detalle == null) {
            throw new IllegalArgumentException("No se encontró información para la reservación: " + numeroReservacion + " de la membresía: " + membresia);
        }

        String estatusClave = detalle.estatusClave() != null ? detalle.estatusClave().trim().toUpperCase() : "";
        String estatusDesc = detalle.estatusDescripcion() != null ? detalle.estatusDescripcion() : estatusClave;

        if (estatusClave.contains("CHECK-IN") || estatusClave.contains("CHECKIN") || estatusClave.contains("CHECK-OUT") || estatusClave.contains("CHECKOUT")) {
            throw new IllegalArgumentException("No se puede generar el formato de registro Check-In porque la reservación ya se encuentra en estatus " + estatusDesc + ".");
        }

        if (!estatusClave.contains("CONFIRMAD") && !estatusClave.contains("PENDIENT")) {
            throw new IllegalArgumentException("La reservación debe estar en estatus CONFIRMADA o PENDIENTE para generar el formato de registro. Estatus actual: " + estatusDesc);
        }

        Integer folioTarjetaId = documentosRepo.spResvGuardarTarjetaRegistro(membresia, numeroReservacion, usuario);

        InformacionSocio socio = null;
        try {
            ApiResponse<InformacionSocio> socioResp = sociosService.obtenerSocios(membresia);
            if (socioResp != null) {
                socio = socioResp.data();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener información complementaria del socio {}: {}", membresia, e.getMessage());
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String nombreCompleto = detalle.nombreHuesped() != null && !detalle.nombreHuesped().isBlank()
                ? detalle.nombreHuesped()
                : (socio != null && socio.nombreCompleto() != null ? socio.nombreCompleto() : "");

        String habitacion = detalle.numeroHabitacion() != null && !detalle.numeroHabitacion().isBlank()
                ? detalle.numeroHabitacion()
                : (detalle.tipoUnidad() != null && !detalle.tipoUnidad().isBlank() ? detalle.tipoUnidad() : "POR ASIGNAR");

        String desarrollo = detalle.nombreDesarrollo() != null && !detalle.nombreDesarrollo().isBlank()
                ? detalle.nombreDesarrollo()
                : (socio != null && socio.desarrollo() != null ? socio.desarrollo() : "CORAL CLUBES");

        String fechaLlegada = detalle.fechaEntrada() != null ? detalle.fechaEntrada().format(dateFormatter) : "";
        String fechaSalida = detalle.fechaSalida() != null ? detalle.fechaSalida().format(dateFormatter) : "";

        String pax = detalle.numeroSocios() != null && detalle.numeroSocios() > 0
                ? detalle.numeroSocios().toString()
                : "1";

        String telefono = socio != null && socio.telefono() != null && !socio.telefono().isBlank()
                ? socio.telefono()
                : (socio != null && socio.telefonoAlternativo() != null && !socio.telefonoAlternativo().isBlank()
                ? socio.telefonoAlternativo()
                : "-----");

        String email = socio != null && socio.correo() != null && !socio.correo().isBlank()
                ? socio.correo()
                : (socio != null && socio.correoAlternativo() != null && !socio.correoAlternativo().isBlank()
                ? socio.correoAlternativo()
                : "-----");

        String direccion = socio != null && socio.direccion() != null ? socio.direccion() : "";

        DesarrolloLogoEnum logoEnum = detalle.desarrolloId() != null
                ? DesarrolloLogoEnum.fromDesarrolloId(detalle.desarrolloId())
                : DesarrolloLogoEnum.fromNombre(desarrollo);

        String logoBase64 = logoEnum.getLogoBase64DataUri();

        Map<String, Object> variables = new HashMap<>();
        variables.put("logoUrl", logoBase64);
        variables.put("desarrollo", desarrollo);
        variables.put("habitacion", habitacion);
        variables.put("socio", membresia);
        variables.put("pax", pax);
        variables.put("folio", folioTarjetaId != null ? folioTarjetaId.toString() : numeroReservacion.toString());
        variables.put("nombre", nombreCompleto);
        variables.put("fechaLlegada", fechaLlegada);
        variables.put("fechaSalida", fechaSalida);
        variables.put("direccion", direccion);
        variables.put("cp", "");
        variables.put("ciudad", "");
        variables.put("pais", "MÉXICO");
        variables.put("telefono", telefono);
        variables.put("email", email);
        variables.put("logoBgColor", logoEnum.getBgColor());

        return pdfGeneratorService.generarPdfDesdeHtml("REGISTRO_CHECKIN", variables);
    }
}
