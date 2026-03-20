package com.coralclubes.facil.shared.infrastructure.security.enums;

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
    CHECKIN_SIN_PAGO("CHECKIN_SIN_PAGO");

    private final String dbClave;
}