package com.coralclubes.facil.modules.reservaciones.engines.cupones_reservaciones.engine;

import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponAccionInstruccion;
import com.coralclubes.facil.modules.reservaciones.dto.response.DetalleReservacionDto;
import com.coralclubes.facil.modules.reservaciones.engines.cupones_reservaciones.interfaces.CuponReservacionBeneficioStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Motor para la orquestación y ejecución de beneficios de cupones específicos del módulo de Reservaciones.
 */
@Slf4j
@Service
public class CuponesReservacionesEngine {

    private final List<CuponReservacionBeneficioStrategy> estrategias;

    public CuponesReservacionesEngine(List<CuponReservacionBeneficioStrategy> estrategias) {
        this.estrategias = estrategias;
        log.info("CuponesReservacionesEngine inicializado con {} estrategias registradas.", estrategias.size());
    }

    /**
     * Procesa una lista de instrucciones de acción generadas por el motor de cupones de Cobranza.
     *
     * @param instrucciones Instrucciones emitidas tras la liquidación del cupón.
     * @param reservacion   Detalle de la reservación.
     * @param usuario       Usuario que autoriza la operación.
     */
    public void procesarInstrucciones(List<CuponAccionInstruccion> instrucciones, DetalleReservacionDto reservacion, String usuario) {
        if (instrucciones == null || instrucciones.isEmpty()) {
            return;
        }

        for (CuponAccionInstruccion instruccion : instrucciones) {
            procesarInstruccion(instruccion, reservacion, usuario);
        }
    }

    /**
     * Procesa una instrucción de acción específica buscando la estrategia correspondiente.
     *
     * @param instruccion Instrucción a procesar.
     * @param reservacion Detalle de la reservación.
     * @param usuario     Usuario que autoriza la operación.
     */
    public void procesarInstruccion(CuponAccionInstruccion instruccion, DetalleReservacionDto reservacion, String usuario) {
        if (instruccion == null) return;

        boolean ejecutada = false;
        for (CuponReservacionBeneficioStrategy estrategia : estrategias) {
            if (estrategia.soporta(instruccion)) {
                log.info("Ejecutando estrategia de reservaciones '{}' para acción '{}' y objetivo '{}' sobre reservación '{} - {}'",
                        estrategia.getClass().getSimpleName(), instruccion.tipoAccion(), instruccion.conceptoObjetivo(),
                        reservacion != null ? reservacion.membresia() : "N/A",
                        reservacion != null ? reservacion.consecutivo() : "N/A");
                estrategia.ejecutarBeneficio(instruccion, reservacion, usuario);
                ejecutada = true;
                break;
            }
        }

        if (!ejecutada) {
            log.info("No se encontró una estrategia en Reservaciones para procesar la instrucción: tipoAccion='{}', conceptoObjetivo='{}'",
                    instruccion.tipoAccion(), instruccion.conceptoObjetivo());
        }
    }
}
