package com.coralclubes.facil.modules.cobranza.engines.cupones.dto;

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
