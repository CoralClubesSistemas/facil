package com.coralclubes.facil.modules.reservaciones.listener;

import com.coralclubes.facil.modules.reservaciones.dto.request.CancelarReservacionRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.ResumenReservacionDto;
import com.coralclubes.facil.modules.reservaciones.service.ReservacionesService;
import com.coralclubes.facil.shared.infrastructure.codes.MovimientosEnum;
import com.coralclubes.facil.shared.events.dto.ReciboCanceladoEvent;
import com.coralclubes.facil.shared.events.dto.ReciboPagadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservacionListener {

    private final ReservacionesService reservacionesService;

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
                        log.info("Detectado movimiento de reservacion (ID: {}). Actualizando reserva...", mov.idMovimiento());

                        // Obtenemos los detalles de la reservacion
                        ResumenReservacionDto reservacion = reservacionesService.obtenerResumenReservacionXMovimiento(event.membresia(), mov.idMovimiento());

                        reservacionesService.actualizarReservacionPagada(
                                reservacion.membresia(),
                                reservacion.consecutivo()
                        );
                    } catch (Exception e) {
                        log.error("Error actualizando reserva para movimiento {}: {}", mov.idMovimiento(), e.getMessage());
                        // Modulith marcará el evento como fallido en la tabla EVENT_PUBLICATION para reintento
                        throw e;
                    }
                });
    }
}