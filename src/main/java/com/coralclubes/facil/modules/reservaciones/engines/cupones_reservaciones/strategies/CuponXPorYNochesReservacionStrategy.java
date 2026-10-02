package com.coralclubes.facil.modules.reservaciones.engines.cupones_reservaciones.strategies;

import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponAccionInstruccion;
import com.coralclubes.facil.modules.cobranza.engines.cupones.strategies.beneficios.BeneficioXPorYStrategy;
import com.coralclubes.facil.modules.reservaciones.dto.response.DetalleReservacionDto;
import com.coralclubes.facil.modules.reservaciones.engines.cupones_reservaciones.interfaces.CuponReservacionBeneficioStrategy;
import com.coralclubes.facil.modules.reservaciones.repository.ReservacionesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Estrategia de reservaciones para ejecutar el beneficio "Paga X y Obtén Y" cuando el objetivo son NOCHES.
 *
 * <p><b>Lógica de Ejecución:</b></p>
 * <ol>
 *   <li>Verifica que la instrucción corresponda a la bonificación de noches de estancia.</li>
 *   <li>Calcula la extensión sumando la cantidad de noches bonificadas a la fecha de salida original:
 *       <pre>nuevaFechaSalida = reservacion.fechaSalida().plusDays(nochesBonificadas)</pre>
 *   </li>
 *   <li>Invoca el procedimiento almacenado {@code spResvModificarFechasReservacion} en base de datos
 *       mediante {@link ReservacionesRepository#spResvModificarFechasReservacion(String, Integer, LocalDate, String, String)}.</li>
 *   <li>Registra en bitácora el evento de ampliación de estancia con los detalles de fechas, noches y membresía.</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CuponXPorYNochesReservacionStrategy implements CuponReservacionBeneficioStrategy {

    public static final String TIPO_ACCION = BeneficioXPorYStrategy.ACCION_BONIFICACION_POSTERIOR;
    public static final String CONCEPTO_OBJETIVO = "NOCHES";

    private final ReservacionesRepository reservacionesRepository;

    @Override
    public String getTipoAccion() {
        return TIPO_ACCION;
    }

    @Override
    public String getConceptoObjetivo() {
        return CONCEPTO_OBJETIVO;
    }

    @Override
    public boolean soporta(CuponAccionInstruccion instruccion) {
        if (instruccion == null) return false;

        boolean accionCompatible = TIPO_ACCION.equalsIgnoreCase(instruccion.tipoAccion())
                || BeneficioXPorYStrategy.CLAVE.equalsIgnoreCase(instruccion.tipoAccion())
                || "BONIFICACION_UNIDADES".equalsIgnoreCase(instruccion.tipoAccion());

        boolean objetivoCompatible = instruccion.conceptoObjetivo() != null &&
                (instruccion.conceptoObjetivo().toUpperCase().contains("NOCHES") ||
                        instruccion.conceptoObjetivo().toUpperCase().contains("RESERVACION"));

        return accionCompatible && objetivoCompatible;
    }

    @Override
    public void ejecutarBeneficio(CuponAccionInstruccion instruccion, DetalleReservacionDto reservacion, String usuario) {
        if (reservacion == null) {
            throw new IllegalArgumentException("El detalle de la reservación no puede ser nulo para aplicar la ampliación de estancia.");
        }

        LocalDate fechaSalidaActual = reservacion.fechaSalida();
        if (fechaSalidaActual == null) {
            throw new IllegalStateException("La reservación no contiene una fecha de salida válida.");
        }

        if (reservacion.membresia() == null || reservacion.membresia().isBlank() || reservacion.consecutivo() == null) {
            throw new IllegalArgumentException("La reservación debe contar con membresía y consecutivo para aplicar la ampliación de estancia.");
        }

        // Obtener la cantidad de noches a bonificar
        int nochesBonificadas = instruccion.cantidad() != null && instruccion.cantidad() > 0
                ? instruccion.cantidad()
                : 1;

        // Calcular la nueva fecha de salida
        LocalDate nuevaFechaSalida = fechaSalidaActual.plusDays(nochesBonificadas);

        String motivo = String.format("Ampliación de estancia por cupón X por Y (%d noches)", nochesBonificadas);

        // Ejecutar en repositorio el SP spResvModificarFechasReservacion
        reservacionesRepository.spResvModificarFechasReservacion(
                reservacion.membresia(),
                reservacion.consecutivo(),
                nuevaFechaSalida,
                usuario,
                motivo
        );

        // Registrar el evento de ampliación en logs
        log.info("EVENTO_AMPLIACION_ESTANCIA: Membresía '{}', Consecutivo '{}', noches bonificadas: {}, fechaSalidaOriginal: {}, nuevaFechaSalida: {}, usuario: '{}', motivo: '{}'",
                reservacion.membresia(),
                reservacion.consecutivo(),
                nochesBonificadas,
                fechaSalidaActual,
                nuevaFechaSalida,
                usuario,
                motivo
        );
    }
}
