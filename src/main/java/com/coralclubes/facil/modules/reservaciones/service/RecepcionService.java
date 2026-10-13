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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
}
