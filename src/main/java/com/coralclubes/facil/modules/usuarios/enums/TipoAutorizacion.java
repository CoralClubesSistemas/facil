package com.coralclubes.facil.modules.usuarios.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoAutorizacion {

    // El Frontend envía estos Nombres ----> Java usa estas claves para el SP
    CREAR_MODULOS("CMDL"),
    VISUALIZAR_MODULOS("RMDL"),
    EDITAR_MODULOS("UMDL"),
    ELIMINAR_MODULOS("DMDL"),
    ELIMINAR_HOTELES("DHTL"),
    ELIMINAR_UNIDADES("DRUN"),
    CHECKIN_SIN_PAGO("CHECKIN_SIN_PAGO"),
    ENVIAR_NOTIFICACIONES("NOTIFICACIONES"),
    AUMENTAR_DESCUENTO_COBRANZA("AUMENTAR_DESCUENTO"),
    DESACTIVAR_ALERTA_COBRANZA("DESACTIVAR_NOTA_COBRANZA"),
    DESACTIVAR_ALERTA_TELECOBRANZA("DESACTIVAR_NOTA_TELECOBRANZA"),
    MODIFICAR_COSTO_TRANSFERENCIA_UNIDADES("MOD_COSTO_TRANSFE"),
    CONCEDER_ACCESO_PREFERENCIAL("ACCESO_PREFERENCIAL");

    private final String dbClave;
}