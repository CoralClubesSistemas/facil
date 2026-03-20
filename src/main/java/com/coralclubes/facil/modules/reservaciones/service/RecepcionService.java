package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.facil.modules.reservaciones.dto.request.*;
import com.coralclubes.facil.modules.reservaciones.dto.response.*;
import com.coralclubes.facil.modules.reservaciones.repository.RecepcionRepository;
import com.coralclubes.facil.modules.reservaciones.repository.ReservacionesRepository;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.NotificationClient;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.responses.codes.GeneralResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecepcionService {

    private final RecepcionRepository repository;
    private final ReservacionesService reservacionesService;
    private final UserContext userContext;
    private final BusinessLogger businessLogger;
    private final NotificationClient notificationClient;
    private final AmaDeLlavesService amaDeLlavesService;

    @Value("${app.clients.notifications.system-code}")
    private String codigoSistemaNotificaciones;

    @Value("${app.clients.notifications.aliases.default}")
    private String aliasConfigNotificaciones;

    @Value("${app.clients.notifications.templates.reserva-cancelada}")
    private String templateReservaCancelada;

    public ApiResponse<List<OperacionDiaDto>> obtenerOperacionesDelDia() {
        Integer desarrolloId = 2;//userContext.getIdDesarrollo();

        if (desarrolloId == null || desarrolloId <= 0) {
            throw new IllegalArgumentException("El ID del desarrollo es obligatorio para consultar la recepción.");
        }

        List<OperacionDiaDto> operaciones = repository.obtenerOperacionesDelDia(desarrolloId);
        return ApiResponse.success("Operaciones del día obtenidas", operaciones);
    }

    public ApiResponse<List<EstadisticaDelDiaDto>> obtenerEstadisticasDelDia() {
        Integer desarrolloId = 2;// userContext.getIdDesarrollo();

        if (desarrolloId == null || desarrolloId <= 0) {
            throw new IllegalArgumentException("El ID del desarrollo es obligatorio.");
        }

        List<EstadisticaDelDiaDto> estadisticas = repository.obtenerEstadisticasDelDia(desarrolloId);
        return ApiResponse.success("Estadísticas del día calculadas", estadisticas);
    }

    public ApiResponse<Boolean> registrarCheckIn(CheckInRequest request) {
        String usuario = userContext.getUsername();
        repository.ejecutarCheckIn(request, usuario);
        return ApiResponse.success("Check-In registrado exitosamente", true);
    }

    /**
     * Registra la salida definitiva del huésped (Check-Out).
     */
    public ApiResponse<Boolean> registrarCheckOut(CheckOutRequest request) {
        String usuario = userContext.getUsername();

        // 1. Obtener el RUN_ID antes del checkout
        ResumenReservacionDto detalle = reservacionesService.obtenerResumenReservacion(request.membresia(), request.consecutivo()).data();

        // 2. Ejecutar Checkout normal
        repository.ejecutarCheckOut(request, usuario);

        // 3. Disparar creación de tarea y WebSocket
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

    public ApiResponse<DetalleReservacionDto> obtenerDetalleReservacion(String membresia, Integer consecutivo) {
        return reservacionesService.obtenerDetalleReservacion(membresia, consecutivo);
    }

    /**
     * Obtiene el catálogo de cuartos físicos disponibles para hacer Check-In.
     */
    public ApiResponse<List<UnidadDisponibleDto>> obtenerUnidadesDisponiblesCheckIn(Integer rhdtId) {
        if (rhdtId == null || rhdtId <= 0) {
            throw new IllegalArgumentException("El ID del tipo de unidad es obligatorio.");
        }

        List<UnidadDisponibleDto> unidades = repository.obtenerUnidadesDisponiblesParaCheckIn(rhdtId);
        return ApiResponse.success("Unidades disponibles obtenidas", unidades);
    }

    /**
     * Consulta el estado de cuenta detallado de una reservación específica.
     */
    public ApiResponse<List<CargoHabitacionDto>> obtenerCargosReservacion(String membresia, Integer consecutivo) {
        return reservacionesService.obtenerCargosReservacion(membresia, consecutivo);
    }

    /**
     * Obtiene el catálogo de cargos disponibles para aplicar a una habitación.
     */
    public ApiResponse<List<CatalogoCargoDto>> obtenerCatalogoCargos(String membresia) {
        if (membresia == null || membresia.isBlank()) {
            throw new IllegalArgumentException("La membresía es obligatoria.");
        }

        List<CatalogoCargoDto> catalogo = repository.obtenerCatalogoCargosHabitacion(membresia);
        return ApiResponse.success("Catálogo de cargos obtenido.", catalogo);
    }

    /**
     * Procesa la inserción de un nuevo cargo financiero durante la estancia del huésped.
     */
    public ApiResponse<Boolean> generarCargo(GenerarCargoRequest request) {
        String usuario = userContext.getUsername();

        repository.generarCargoHabitacion(request, usuario);

        return ApiResponse.success("Cargo aplicado a la habitación exitosamente.", true);
    }

    /**
     * Obtiene el mapa completo de unidades (Rack) para el dashboard de recepción.
     */
    public ApiResponse<List<MapaUnidadDto>> obtenerMapaUnidades() {
        // Obtenemos el Desarrollo del usuario logueado
        Integer desarrolloId = 2; //userContext.getIdDesarrollo();

        List<MapaUnidadDto> mapa = repository.obtenerMapaUnidades(desarrolloId);
        return ApiResponse.success("Mapa de unidades obtenido con éxito.", mapa);
    }

    /**
     * Obtiene el historial de actividad reciente del usuario/desarrollo para el dashboard.
     */
    public ApiResponse<List<String>> obtenerActividadReciente() {
        Integer desarrolloId = 2; //userContext.getIdDesarrollo();

        List<String> actividad = repository.obtenerActividadDiaria(desarrolloId);
        return ApiResponse.success("Actividad reciente obtenida.", actividad);
    }

    /**
     * Procesa la transferencia o upgrade de una habitación en uso.
     */
    public ApiResponse<Boolean> transferirUnidad(TransferirUnidadRequest request) {
        String usuario = userContext.getUsername();

        boolean exec = repository.transferirUnidad(request, usuario);

        if (exec) {
            businessLogger.info(usuario, "Transferencia de habitación realizada para Membresía: {}, Consecutivo: {}, Nuevo RHDT ID: {}, Nuevo RUN ID: {}, Importe Diferencia: {}",
                    request.membresia(), request.consecutivo(), request.nuevoRhdtId(), request.nuevoRunId(), request.importeDiferencia());

            return ApiResponse.success("La transferencia de habitación se realizó con éxito.", true);
        } else {
            businessLogger.error(usuario, "Error al transferir habitación para Membresía: {}, Consecutivo: {}, Nuevo RHDT ID: {}, Nuevo RUN ID: {}, Importe Diferencia: {}",
                    request.membresia(), request.consecutivo(), request.nuevoRhdtId(), request.nuevoRunId(), request.importeDiferencia());

            return ApiResponse.error(GeneralResponseCode.CONFLICT, "Ocurrió un error al realizar la transferencia de habitación. Por favor, inténtelo de nuevo.");
        }
    }

    public ApiResponse<BigDecimal> calcularDiferenciaTransferencia(String membresia, Integer consecutivo, Integer nuevoRhdtId) {
        // 1. Obtenemos los detalles de la reservacion actual
        DetalleReservacionDto detalle = reservacionesService.obtenerDetalleReservacion(membresia, consecutivo).data();

        // 2. Obtenemos la tarifa de la unidad solicitada
        DisponibilidadUnidadDto disponibilidad = reservacionesService.obtenerDisponibilidadUnidad(
                nuevoRhdtId,
                detalle.membresia(),
                detalle.fechaEntrada(),
                detalle.fechaSalida()
        ).data();

        // se valida que la nueva unidad soporte la cantidad de personas de la reservacion original
        BigDecimal diferencia = getBigDecimal(disponibilidad, detalle);

        // validamos que la diferencia no sea negativa
        if (diferencia.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La nueva unidad seleccionada tiene una tarifa menor a la actual. Por favor, seleccione una unidad con tarifa igual o mayor.");
        }

        return ApiResponse.success("Diferencia de tarifa calculada.", diferencia);
    }

    private static BigDecimal getBigDecimal(DisponibilidadUnidadDto disponibilidad, DetalleReservacionDto detalle) {
        if (disponibilidad.capacidad() < detalle.numeroSocios()) {
            throw new IllegalArgumentException("La nueva unidad no tiene la capacidad suficiente para alojar a la cantidad de personas de la reservación original.");
        }

        // 3. Calculamos la diferencia entre la tarifa actual y la nueva
        BigDecimal tarifaActual = detalle.importeTotal();
        BigDecimal tarifaNueva = disponibilidad.costoEstancia();

        BigDecimal diferencia = tarifaNueva.subtract(tarifaActual);
        return diferencia;
    }

    /**
     * Obtiene el monto de penalización previo a la confirmación de la cancelación.
     */
    public ApiResponse<BigDecimal> calcularPenalizacionCancelacion(String membresia, Integer consecutivo) {
        BigDecimal penalizacion = repository.calcularPenalizacionCancelacion(membresia, consecutivo);
        return ApiResponse.success("Cálculo de penalización completado.", penalizacion);
    }

    /**
     * Procesa la cancelación de una reservación activa.
     */
    public ApiResponse<Boolean> cancelarReservacion(CancelarReservacionRequest request) {
        String usuario = userContext.getUsername();

        ResumenReservacionDto detalle = reservacionesService.obtenerResumenReservacion(request.membresia(), request.consecutivo()).data();
        Integer idUnidadFisica = detalle.idUnidadFisica();

        boolean exec = repository.cancelarReservacion(request, usuario);

        if (exec) {
            businessLogger.info(usuario, "Cancelación de reservación realizada para Membresía: {}, Consecutivo: {}, Motivo: {}, Cobro Penalización: {}",
                    request.membresia(), request.consecutivo(), request.motivoCancelacion(), request.cobrarCuotaCancelacion());

            BigDecimal costoCancelacion = request.cobrarCuotaCancelacion() ? repository.calcularPenalizacionCancelacion(request.membresia(), request.consecutivo()) : BigDecimal.ZERO;

            enviarCorreoCancelacion(detalle, costoCancelacion);

            if (idUnidadFisica != null && idUnidadFisica > 0) {
                amaDeLlavesService.crearTareaYNotificar(
                        idUnidadFisica,
                        detalle.numeroUnidad(),
                        detalle.desarrolloId(),
                        usuario,
                        "CANCELACIÓN DE RESERVACIÓN"
                );
            }

            return ApiResponse.success("La reservación ha sido cancelada y la habitación liberada con éxito.", true);
        } else {
            businessLogger.error(usuario, "Error al cancelar reservación para Membresía: {}, Consecutivo: {}",
                    request.membresia(), request.consecutivo());

            return ApiResponse.error(GeneralResponseCode.CONFLICT, "Ocurrió un error al realizar la cancelación de la reservación. Por favor, inténtelo de nuevo.");
        }
    }

    private void enviarCorreoCancelacion(ResumenReservacionDto detalle, BigDecimal costoCancelacion) {
        // Preparamos las variables para el correo
        String destinatario = detalle.emailContacto();
        String nombreCliente = detalle.nombreContacto();
        String membresia = detalle.membresia();
        Integer consecutivo = detalle.consecutivo();

        Map<String, Object> urlVariables = new HashMap<>();
        urlVariables.put("membresia", membresia);
        urlVariables.put("folio", consecutivo);
        urlVariables.put("nombreTitular", nombreCliente);
        urlVariables.put("costoCancelacion", costoCancelacion.compareTo(BigDecimal.ZERO) > 0 ? costoCancelacion.toString() : null);

        // Contruimos el payload para la notificación
        // 5. Construir Solicitud a Coral Notificaciones
        SolicitudNotificacionDto solicitudNotificacion = SolicitudNotificacionDto.builder()
                .codigoSistema(codigoSistemaNotificaciones)
                .aliasConfig(aliasConfigNotificaciones)
                .destinatarios(List.of(destinatario))
                .codigoPlantilla(templateReservaCancelada)
                .variables(urlVariables)
                .prioridad(10)
                .build();

        // 6. Enviar a Cola RabbitMQ (Fire and Forget)
        notificationClient.enviarNotificacion(solicitudNotificacion);

    }
}