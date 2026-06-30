package com.coralclubes.facil.modules.reservaciones.listener;

import com.coralclubes.facil.modules.cobranza.dto.response.IntentoPagoDto;
import com.coralclubes.facil.modules.cobranza.repository.IntentoPagoRepository;
import com.coralclubes.facil.modules.cobranza.service.CobranzaService;
import com.coralclubes.facil.modules.reservaciones.dto.request.CancelarReservacionRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.ConfirmarReservaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.DetallePagoCheckoutRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.ResumenReservacionDto;
import com.coralclubes.facil.modules.reservaciones.service.ReservacionesService;
import com.coralclubes.facil.shared.infrastructure.codes.MovimientosEnum;
import com.coralclubes.facil.shared.events.dto.ReciboCanceladoEvent;
import com.coralclubes.facil.shared.events.dto.ReciboPagadoEvent;
import com.coralclubes.facil.shared.events.dto.ReservacionConfirmadaEvent;
import com.coralclubes.utils.json.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservacionListener {

    private final ReservacionesService reservacionesService;
    private final CobranzaService cobranzaService;
    private final IntentoPagoRepository intentoPagoRepository;

    /**
     * Este método escucha el evento de cancelación de recibo.
     * Asegura que el evento se procese solo si la transacción de Cobranza hizo COMMIT.
     */
    @ApplicationModuleListener
    public void handleReciboCancelado(ReciboCanceladoEvent event) {
        // 1. Leemos la decisión global una sola vez (si no viene, por defecto es false)
        Boolean cancelarReservas = event.decisionesUsuario() != null &&
                event.decisionesUsuario().getOrDefault("CANCELAR_RESERVAS", false);

        event.movimientosAfectados().stream()
                .filter(mov -> Objects.equals(mov.tipoMovimiento(), MovimientosEnum.RESERVACIONES.getId()))
                .forEach(mov -> {
                    try {
                        ResumenReservacionDto reservacion = reservacionesService.obtenerResumenReservacionXMovimiento(event.membresia(), mov.idMovimiento());

                        // 2. Protegemos las reservas que por regla de negocio NO pueden ser canceladas
                        // aunque la instrucción global sea true.
                        boolean esCancelable = !reservacion.estatusClave().equals("CHECK-IN") &&
                                !reservacion.estatusClave().equals("CHECK-OUT");

                        if (cancelarReservas && esCancelable) {
                            log.info("Cancelando reservación {} por decisión global del usuario...", reservacion.consecutivo());
                            reservacionesService.cancelarReservacion(new CancelarReservacionRequest(
                                    reservacion.membresia(),
                                    reservacion.consecutivo(),
                                    event.motivoCancelacion(),
                                    false
                            ), event.usuario());
                        } else {
                            log.info("La reservación {} se mantuvo activa (estatus: {} o decisión general: false)",
                                    reservacion.consecutivo(), reservacion.estatusClave());
                        }
                    } catch (Exception e) {
                        log.error("Error procesando evento de cancelación para reserva {}: {}", mov.idMovimiento(), e.getMessage());
                        throw e;
                    }
                });
    }

    /**
     * Este método escucha el evento de PAGO de recibo.
     */
    @ApplicationModuleListener
    public void handleReciboPagado(ReciboPagadoEvent event) {
        log.info("Analizando movimientos pagados para la membresía: {}", event.membresia());

        event.movimientosAfectados().stream()
                // Si el tipo de movimiento es de reservacion
                .filter(mov -> Objects.equals(mov.tipoMovimiento(), MovimientosEnum.RESERVACIONES.getId()))
                .forEach(mov -> {
                    try {
                        log.info("Detectado movimiento de reservación (ID: {}). Procesando...", mov.idMovimiento());

                        // 1. Verificar si la reservación física ya existe
                        Optional<ResumenReservacionDto> reservacionOpt = reservacionesService.buscarResumenReservacionXMovimiento(event.membresia(), mov.idMovimiento());

                        if (reservacionOpt.isPresent()) {
                            // Flujo normal: ya existe, solo actualizamos a PAGADA
                            ResumenReservacionDto reservacion = reservacionOpt.get();
                            log.info("La reservación física ya existe (Consecutivo: {}). Actualizando estatus a PAGADA...", reservacion.consecutivo());
                            reservacionesService.actualizarReservacionPagada(
                                    reservacion.membresia(),
                                    reservacion.consecutivo()
                            );
                        } else {
                            // Flujo portal web/checkout: la reservación física no ha sido creada aún.
                            // La creamos a partir de la orden de cobranza y los intentos de pago.
                            log.info("La reservación física no existe aún para movimiento ID: {}. Iniciando creación física...", mov.idMovimiento());

                            if (event.ordenUuid() == null || event.ordenUuid().isBlank()) {
                                log.error("El evento de recibo pagado no contiene un UUID de orden. Omitiendo materialización.");
                                return;
                            }
                            UUID ordenUuid = UUID.fromString(event.ordenUuid());

                            // Obtener los intentos de pago
                            List<IntentoPagoDto> intentos = intentoPagoRepository.spCobranzaObtenerIntentosPagoPorOrden(ordenUuid);

                            // Buscar el intento de pago LINK aprobado
                            Optional<IntentoPagoDto> intentoOpt = intentos.stream()
                                    .filter(i -> "LINK".equalsIgnoreCase(i.formaPagoClave()))
                                    .filter(i -> "APROBADO".equalsIgnoreCase(i.estatus()))
                                    .findFirst();

                            if (intentoOpt.isPresent()) {
                                IntentoPagoDto intento = intentoOpt.get();
                                if (intento.metadata() != null && !intento.metadata().isBlank()) {
                                    Map<String, Object> meta = JsonUtils.fromJson(intento.metadata(), Map.class);

                                    ConfirmarReservaRequest originalRequest = JsonUtils.fromJson(
                                            JsonUtils.toJson(meta.get("request")), ConfirmarReservaRequest.class
                                    );

                                    List<DetallePagoCheckoutRequest> detallePago = JsonUtils.fromJson(
                                            JsonUtils.toJson(meta.get("detallePago")),
                                            new TypeReference<List<DetallePagoCheckoutRequest>>() {}
                                    );

                                    log.info("Materializando reservación física desde el Listener para membresía: {}", originalRequest.membresia());

                                    // Llamamos al SP para crear la reservación física y asociarla
                                    List<com.coralclubes.facil.modules.reservaciones.dto.response.ReservacionCreadaDetalle> reservaciones = reservacionesService.confirmarYCrearReservacion(
                                            originalRequest.groupId(),
                                            originalRequest.email(),
                                            originalRequest.email2(),
                                            originalRequest.telefono1(),
                                            originalRequest.telefono2(),
                                            originalRequest.nombreReserva(),
                                            originalRequest.peticionEspecial(),
                                            "INTERNET",
                                            detallePago
                                    );
                                    log.info("Reservación física materializada con éxito desde ReciboPagadoEvent.");

                                    // Generar y enviar la carta de ocupación para cada reservación física creada
                                    for (com.coralclubes.facil.modules.reservaciones.dto.response.ReservacionCreadaDetalle rcd : reservaciones) {
                                        try {
                                            log.info("Generando y enviando carta de ocupación para folio: {}", rcd.reservacionConsecutivo());
                                            ReservacionConfirmadaEvent resEvent = reservacionesService.construirEventDesdeDb(event.membresia(), rcd.reservacionConsecutivo());
                                            UUID uuidCarta = reservacionesService.generarYPersistirCartaOcupacion(resEvent);
                                            reservacionesService.enviarNotificacionCartaOcupacion(resEvent, uuidCarta, List.of());
                                            log.info("Carta de ocupación enviada correctamente para consecutivo: {}", rcd.reservacionConsecutivo());
                                        } catch (Exception ex) {
                                            log.error("Error al enviar la carta de ocupación para folio " + rcd.reservacionConsecutivo() + ": " + ex.getMessage(), ex);
                                        }
                                    }
                                }
                            } else {
                                log.error("No se encontró ningún intento de pago LINK APROBADO para la orden: {}", ordenUuid);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error procesando evento de pago para movimiento {}: {}", mov.idMovimiento(), e.getMessage());
                        // Modulith marcará el evento como fallido en la tabla EVENT_PUBLICATION para reintento
                        throw e;
                    }
                });
    }
}