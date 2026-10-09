package com.coralclubes.facil.shared.domain.enums;

public enum ClavesModulos {
    AMADELLAVES("smnuHousekeeping");

    private final String clave;

    ClavesModulos(String clave) {
        this.clave = clave;
    }

    public String getClave() {
        return clave;
    }
}
