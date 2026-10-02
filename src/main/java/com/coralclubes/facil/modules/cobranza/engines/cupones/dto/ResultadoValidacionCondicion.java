package com.coralclubes.facil.modules.cobranza.engines.cupones.dto;

/**
 * Resultado de la validación de una condición.
 *
 * @param esValida      Indica si la condición es válida.
 * @param mensajeRechazo El mensaje de rechazo en caso de que la condición no sea válida.
 */
public record ResultadoValidacionCondicion(
        boolean esValida,
        String mensajeRechazo
) {
    public static ResultadoValidacionCondicion valida() {
        return new ResultadoValidacionCondicion(true, null);
    }

    public static ResultadoValidacionCondicion invalida(String mensaje) {
        return new ResultadoValidacionCondicion(false, mensaje);
    }
}
