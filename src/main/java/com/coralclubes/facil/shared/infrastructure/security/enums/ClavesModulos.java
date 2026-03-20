package com.coralclubes.facil.shared.infrastructure.security.enums;

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
