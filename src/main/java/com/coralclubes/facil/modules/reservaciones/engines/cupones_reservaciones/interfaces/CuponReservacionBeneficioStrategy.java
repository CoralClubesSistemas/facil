package com.coralclubes.facil.modules.reservaciones.engines.cupones_reservaciones.interfaces;

import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponAccionInstruccion;
import com.coralclubes.facil.modules.reservaciones.dto.response.DetalleReservacionDto;

/**
 * Estrategia de procesamiento para acciones e instrucciones de cupones aplicados al módulo de Reservaciones.
 */
public interface CuponReservacionBeneficioStrategy {

    /**
     * Identificador del tipo de acción o beneficio que maneja esta estrategia
     * (ej. "BONIFICACION_UNIDADES_POSTERIOR", "X_POR_Y", etc.).
     */
    String getTipoAccion();

    /**
     * Identificador del concepto u objetivo que maneja esta estrategia
     * (ej. "NOCHES", "RESERVACION", etc.).
     */
    String getConceptoObjetivo();

    /**
     * Determina si esta estrategia puede procesar la instrucción dada.
     *
     * @param instruccion Instrucción emitida por el motor de cupones.
     * @return true si aplica a la instrucción, false en caso contrario.
     */
    default boolean soporta(CuponAccionInstruccion instruccion) {
        if (instruccion == null) return false;
        boolean coincideAccion = getTipoAccion() == null || getTipoAccion().equalsIgnoreCase(instruccion.tipoAccion());
        boolean coincideObjetivo = getConceptoObjetivo() == null || (instruccion.conceptoObjetivo() != null &&
                instruccion.conceptoObjetivo().toUpperCase().contains(getConceptoObjetivo().toUpperCase()));
        return coincideAccion && coincideObjetivo;
    }

    /**
     * Ejecuta el beneficio/acción postprocesamiento sobre el detalle de la reservación.
     *
     * @param instruccion Instrucción emitida por el motor de cobranza.
     * @param reservacion Detalle de la reservación sobre la que se aplica la acción.
     * @param usuario     Usuario que autoriza o realiza la operación.
     */
    void ejecutarBeneficio(CuponAccionInstruccion instruccion, DetalleReservacionDto reservacion, String usuario);
}
