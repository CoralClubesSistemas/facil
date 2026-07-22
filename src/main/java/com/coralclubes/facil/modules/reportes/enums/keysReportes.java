package com.coralclubes.facil.modules.reportes.enums;

public enum keysReportes {
    ACCESOS_MEMBRESIA("ACCESOS_MEMBRESIA"),
    HISTORICO_MOV_MEM("HISTORICO_MOV_MEM");

    private final String clave;

    keysReportes(String clave) {
        this.clave = clave;
    }

    public String getClave() {
        return clave;
    }
}
