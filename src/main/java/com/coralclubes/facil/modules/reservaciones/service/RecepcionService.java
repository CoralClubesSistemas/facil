package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GenerarOrdenCobranzaRequest;
import com.coralclubes.facil.modules.cobranza.service.CobranzaService;
import com.coralclubes.facil.modules.reservaciones.dto.projection.DisponibilidadUnidadProjection;
import com.coralclubes.facil.modules.reservaciones.dto.request.*;
import com.coralclubes.facil.modules.reservaciones.dto.response.*;
import com.coralclubes.facil.modules.reservaciones.repository.RecepcionRepository;
import com.coralclubes.facil.modules.reservaciones.repository.ReservacionesRepository;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.responses.codes.GeneralResponseCode;
import com.coralclubes.facil.shared.platform.qr.dto.SystemQrResolucionResponse;
import com.coralclubes.facil.shared.platform.qr.enums.SystemQrStatus;
import com.coralclubes.facil.shared.platform.qr.service.SystemQrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecepcionService {

    private final RecepcionRepository recepcionRepo;
    private final ReservacionesRepository reservacionesRepo;
    private final AmaDeLlavesService amaDeLlavesService;
    private final CobranzaService cobranzaService;
    private final SystemQrService systemQrService;
    private final BusinessLogger businessLogger;
    private final UserContext userContext;

    public ApiResponse<List<OperacionDiaDto>> obtenerOperacionesDelDia() {
        Integer desarrolloId = userContext.getIdDesarrollo();

        if (desarrolloId == null || desarrolloId <= 0) {
            throw new IllegalArgumentException("El desarrollo asignado a su usuario no permite el acceso a esta información. Contacte al administrador del sistema.");
        }

        List<OperacionDiaDto> operaciones = recepcionRepo.obtenerOperacionesDelDia(desarrolloId);
        return ApiResponse.success("Operaciones del día obtenidas", operaciones);
    }

    public ApiResponse<List<EstadisticaDelDiaDto>> obtenerEstadisticasDelDia() {
        Integer desarrolloId = userContext.getIdDesarrollo();

        if (desarrolloId == null || desarrolloId <= 0) {
            throw new IllegalArgumentException("El desarrollo asignado a su usuario no permite el acceso a esta información. Contacte al administrador del sistema.");
        }

        List<EstadisticaDelDiaDto> estadisticas = recepcionRepo.obtenerEstadisticasDelDia(desarrolloId);
        return ApiResponse.success("Estadísticas del día calculadas", estadisticas);
    }

    public ApiResponse<Boolean> registrarCheckIn(CheckInRequest request) {
        String usuario = userContext.getUsername();
        recepcionRepo.ejecutarCheckIn(request, usuario);

        // Consumir un uso del código QR activo asociado a la reservación si existe
        try {
            String exactEntityId = request.membresia() + ":" + request.consecutivo();
            String pattern = request.membresia() + ":%" + request.consecutivo() + "%";
            String metadataJson = String.format("{\"folio\":%s,\"idUnidad\":%s}", request.consecutivo(), request.idUnidad());

            systemQrService.consumirUsoQrPorEntidad(
                    "RESERVACIONES",
                    "RESERVATION",
                    exactEntityId,
                    pattern,
                    "CHECK_IN",
                    usuario,
                    null,
                    metadataJson
            );
        } catch (Exception ex) {
            log.warn("No fue posible consumir uso de QR para reservación {}:{}: {}",
                    request.membresia(), request.consecutivo(), ex.getMessage());
        }

        return ApiResponse.success("Check-In registrado exitosamente", true);
    }

    public ApiResponse<Boolean> registrarCheckOut(CheckOutRequest request) {
        String usuario = userContext.getUsername();

        ResumenReservacionDto detalle = reservacionesRepo.obtenerResumenReservacion(request.membresia(), request.consecutivo());

        recepcionRepo.ejecutarCheckOut(request, usuario);

        if (detalle.idUnidadFisica() != null) {
            amaDeLlavesService.crearTareaYNotificar(
                    detalle.idUnidadFisica(),
                    detalle.numeroUnidad(),
                    detalle.desarrolloId(),
                    usuario,
                    "CHECK-OUT"
            );
        }

        return ApiResponse.success("Check-Out registrado exitosamente. La habitación ha sido liberada.", true);
    }

    public ApiResponse<List<UnidadDisponibleDto>> obtenerUnidadesDisponiblesCheckIn(String membresia, Integer consecutivo, Integer rhdtId) {
        if (rhdtId == null || rhdtId <= 0) {
            throw new IllegalArgumentException("El ID del tipo de unidad es obligatorio.");
        }
        if (membresia == null || membresia.isBlank() || consecutivo == null) {
            throw new IllegalArgumentException("La membresía y el consecutivo son obligatorios para validar la disponibilidad real.");
        }

        List<UnidadDisponibleDto> unidades = recepcionRepo.obtenerUnidadesDisponiblesParaCheckIn(membresia, consecutivo, rhdtId);
        return ApiResponse.success("Unidades disponibles obtenidas", unidades);
    }

    public ApiResponse<CheckInOutEspecialCotizacionDto> cotizarCheckInOutEspecial(String membresia, Integer consecutivo) {
        if (membresia == null || membresia.isBlank() || consecutivo == null) {
            throw new IllegalArgumentException("La membresía y el consecutivo son obligatorios.");
        }

        CheckInOutEspecialCotizacionDto cotizacion = recepcionRepo.cotizarCheckInOutEspecial(membresia, consecutivo);

        if (cotizacion == null) {
            return ApiResponse.error(GeneralResponseCode.NOT_FOUND, "No se encontró la reservación especificada.");
        }

        return ApiResponse.success("Cotización de operación especial obtenida.", cotizacion);
    }

    public ApiResponse<Boolean> registrarMovimientoCheckInOutEspecial(CheckInOutEspecialRequest request) {
        String usuario = userContext.getUsername();

        recepcionRepo.registrarMovimientoCheckInOutEspecial(request, usuario);

        businessLogger.info(usuario, "Cargo por {} registrado: Membresía {}, Consecutivo {}",
                request.tipoOperacion(), request.membresia(), request.consecutivo());
        return ApiResponse.success("Cargo por " + request.tipoOperacion().name() + " registrado correctamente.", true);
    }

    public ApiResponse<List<CatalogoCargoDto>> obtenerCatalogoCargos(String membresia) {
        if (membresia == null || membresia.isBlank()) {
            throw new IllegalArgumentException("La membresía es obligatoria.");
        }

        List<CatalogoCargoDto> catalogo = recepcionRepo.obtenerCatalogoCargosHabitacion(membresia);
        return ApiResponse.success("Catálogo de cargos obtenido.", catalogo);
    }

    public ApiResponse<Boolean> generarCargo(GenerarCargoRequest request) {
        String usuario = userContext.getUsername();

        recepcionRepo.generarCargoHabitacion(request, usuario);

        return ApiResponse.success("Cargo aplicado a la habitación exitosamente.", true);
    }

    public ApiResponse<List<MapaUnidadDto>> obtenerMapaUnidades() {
        Integer desarrolloId = userContext.getIdDesarrollo();

        List<MapaUnidadDto> mapa = recepcionRepo.obtenerMapaUnidades(desarrolloId);
        return ApiResponse.success("Mapa de unidades obtenido con éxito.", mapa);
    }

    public ApiResponse<Boolean> transferirUnidad(TransferirUnidadRequest request) {
        String usuario = userContext.getUsername();

        ResumenReservacionDto detalle = reservacionesRepo.obtenerResumenReservacion(request.membresia(), request.consecutivo());

        recepcionRepo.transferirUnidad(request, usuario);

        businessLogger.info(usuario, "Transferencia de habitación realizada para Membresía: {}, Consecutivo: {}, Nuevo RHDT ID: {}, Nuevo RUN ID: {}, Importe Diferencia: {}, Marcar Unidad Anterior para Limpieza: {}",
                request.membresia(), request.consecutivo(), request.nuevoRhdtId(), request.nuevoRunId(), request.importeDiferencia(), request.limpiarUnidadAnterior());

        if (detalle.idUnidadFisica() != null && request.limpiarUnidadAnterior()) {
            amaDeLlavesService.crearTareaYNotificar(
                    detalle.idUnidadFisica(),
                    detalle.numeroUnidad(),
                    detalle.desarrolloId(),
                    usuario,
                    "TRANSFERENCIA-HABITACION"
            );
        }

        return ApiResponse.success("La transferencia de habitación se realizó con éxito.", true);
    }

    public ApiResponse<BigDecimal> calcularDiferenciaTransferencia(String membresia, Integer consecutivo, Integer nuevoRhdtId) {
        DetalleReservacionDto detalle = reservacionesRepo.obtenerDetalleReservacion(membresia, consecutivo);

        DisponibilidadUnidadProjection projection = reservacionesRepo.obtenerDisponibilidadUnidadEspecifica(
                nuevoRhdtId,
                detalle.fechaEntrada(),
                detalle.fechaSalida(),
                detalle.membresia()
        );

        if (projection == null || projection.costoEstancia() == null) {
            throw new IllegalArgumentException("No se encontró tarifa definida para la nueva unidad seleccionada.");
        }

        BigDecimal costoNuevaUnidad = projection.costoEstancia();
        BigDecimal costoActual = detalle.importeTotal() != null ? detalle.importeTotal() : BigDecimal.ZERO;
        BigDecimal diferencia = costoNuevaUnidad.subtract(costoActual);

        if (diferencia.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La nueva unidad seleccionada tiene una tarifa menor a la actual. Por favor, seleccione una unidad con tarifa igual o mayor.");
        }

        return ApiResponse.success("Diferencia de tarifa calculada.", diferencia);
    }

    public UUID generarOrdenSaldosPendientes(String membresia, Integer folio) {
        List<GenerarOrdenCobranzaMovimientoRequest> movimientos = reservacionesRepo.obtenerCargosReservacion(membresia, folio).stream()
                .filter(cargo -> cargo.importePendiente().compareTo(BigDecimal.ZERO) > 0)
                .map(cargo -> GenerarOrdenCobranzaMovimientoRequest.builder()
                        .idMovimiento(cargo.idMovimiento())
                        .montoCapital(cargo.importePendiente())
                        .montoInteres(BigDecimal.ZERO)
                        .interesPago(BigDecimal.ZERO)
                        .interesesBonificados(BigDecimal.ZERO)
                        .totalDescuento(BigDecimal.ZERO)
                        .justificacionDescuento(null)
                        .usuarioAutoriza(userContext.getUsername())
                        .build())
                .toList();

        var ordenRequest = GenerarOrdenCobranzaRequest.builder()
                .membresia(membresia)
                .movimientos(movimientos)
                .agregarIva(false)
                .ivaIncluido(false)
                .build();

        return cobranzaService.generarOrdenCobranza(ordenRequest, userContext.getUsername()).data().ordenUuid();
    }

    public ResolucionQrCheckInResponse resolverQrParaCheckIn(String token, Integer usuarioDesarrollo) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El token del código QR es obligatorio.");
        }

        // 1. Obtener y resolver el token mediante el servicio de QR global
        SystemQrResolucionResponse qr = systemQrService.resolverQrPorToken(token);

        // 2. Validar estatus permitido (Se aceptan ACTIVE y USED; otros como EXPIRED o REVOKED lanzan error)
        if (qr.status() != SystemQrStatus.ACTIVE && qr.status() != SystemQrStatus.USED) {
            throw new IllegalArgumentException("El código QR proporcionado no es válido (estado actual: " + qr.status() + ").");
        }

        // 3. Validar que corresponda al dominio de reservaciones
        boolean esModuloValido = (qr.module() != null && qr.module().equalsIgnoreCase("RESERVACIONES"))
                || (qr.entityType() != null && qr.entityType().equalsIgnoreCase("RESERVATION"));

        if (!esModuloValido) {
            throw new IllegalArgumentException("El código QR no corresponde a una reservación válida.");
        }

        String entityId = qr.entityId();
        if (entityId == null || entityId.isBlank()) {
            throw new IllegalArgumentException("El código QR no contiene información de reservación vinculada.");
        }

        // 4. Parsear entityId (Formato: membresia:consecutivo1:consecutivo2 o membresia:consecutivo1,consecutivo2)
        String[] partes = entityId.split(":", 2);
        String membresia = partes[0].trim();

        if (partes.length < 2 || partes[1].isBlank()) {
            throw new IllegalArgumentException("El código QR no contiene folios de reservación válidos.");
        }

        // Los folios restantes pueden venir separados por ':' o por ','
        String[] tokensFolios = partes[1].split("[: ,]+");
        List<Integer> folios = new ArrayList<>();
        for (String fStr : tokensFolios) {
            fStr = fStr.trim();
            if (!fStr.isEmpty()) {
                try {
                    folios.add(Integer.parseInt(fStr));
                } catch (NumberFormatException ignored) {
                    log.warn("Se ignoró segmento no numérico en folios de QR: {}", fStr);
                }
            }
        }

        if (folios.isEmpty()) {
            throw new IllegalArgumentException("No se pudieron extraer folios de reservación válidos del código QR.");
        }

        // 5. Consultar los detalles de cada reservación
        List<DetalleReservacionDto> reservaciones = new ArrayList<>();
        for (Integer folio : folios) {
            try {
                DetalleReservacionDto detalle = reservacionesRepo.obtenerDetalleReservacion(membresia, folio);
                if (detalle != null) {
                    reservaciones.add(detalle);
                }
            } catch (Exception ex) {
                log.warn("No fue posible obtener el detalle para la reservación {}:{} desde QR: {}", membresia, folio, ex.getMessage());
            }
        }

        if (reservaciones.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron reservaciones para los folios indicados en el código QR.");
        }

        // Validar que todas las reservaciones pertenezcan al desarrollo del usuario
        for (DetalleReservacionDto reservacion : reservaciones) {
            if (usuarioDesarrollo != null && reservacion.desarrolloId() != null && !reservacion.desarrolloId().equals(usuarioDesarrollo)) {
                throw new IllegalArgumentException("Una o más reservaciones del código QR no pertenecen al desarrollo asignado a su usuario.");
            }
        }

        int maxUses = qr.maxUses() != null ? qr.maxUses() : 1;
        int usedCount = qr.usedCount() != null ? qr.usedCount() : 0;
        int usosDisponibles = Math.max(0, maxUses - usedCount);

        boolean consumible = qr.status() == SystemQrStatus.ACTIVE && usosDisponibles > 0;
        String mensaje;

        if (qr.status() == SystemQrStatus.USED || usosDisponibles == 0) {
            mensaje = "El código QR ya ha sido utilizado en su totalidad y no cuenta con check-in(s) disponibles.";
        } else {
            mensaje = String.format("Código QR válido. Check-in(s) disponible(s): %d de %d.", usosDisponibles, maxUses);
        }

        log.debug("QR resuelto para membresía {} con folios {}. Consumible: {}, Usos disponibles: {}/{}",
                membresia, folios, consumible, usosDisponibles, maxUses);

        return ResolucionQrCheckInResponse.builder()
                .token(token)
                .membresia(membresia)
                .status(qr.status())
                .consumible(consumible)
                .maxUses(maxUses)
                .usedCount(usedCount)
                .usosDisponibles(usosDisponibles)
                .mensaje(mensaje)
                .reservaciones(reservaciones)
                .build();
    }
}
