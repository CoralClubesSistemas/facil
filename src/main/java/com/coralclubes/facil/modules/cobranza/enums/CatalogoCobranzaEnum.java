package com.coralclubes.facil.modules.cobranza.enums;

import lombok.Getter;

@Getter
public enum CatalogoCobranzaEnum {
    TEMPORADA("temporada", "spCobranzaCatalogoTemporadas"),
    DIAS("dias", "spCobranzaCatalogoDias");

    private final String clave;
    private final String spName;

    CatalogoCobranzaEnum(String clave, String spName) {
        this.clave = clave;
        this.spName = spName;
    }

    public static CatalogoCobranzaEnum fromClave(String clave) {
        for (CatalogoCobranzaEnum item : values()) {
            if (item.clave.equalsIgnoreCase(clave) || item.name().equalsIgnoreCase(clave)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Clave de catálogo no válida: " + clave);
    }
}
