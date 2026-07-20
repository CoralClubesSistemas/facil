package com.coralclubes.facil.modules.reportes.enums;

public enum keysReportes {
    ACCESOS_MEMBRESIA("ACCESOS_MEMBRESIA");

    private final String clave;

    keysReportes(String clave) {
        this.clave = clave;
    }

    public String getClave() {
        return clave;
    }
}
