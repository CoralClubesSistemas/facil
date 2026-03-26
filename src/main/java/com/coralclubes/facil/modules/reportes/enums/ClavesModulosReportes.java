package com.coralclubes.facil.modules.reportes.enums;

public enum ClavesModulosReportes {

    COBRANZA("ssmnuRepIntCobranza"),
    RESERVACIONES("ssmnuRepIntReservaciones"),
    DATOS_GENERALES("ssmnuRepIntDatosGenerales"),
    EVENTOS("ssmnuRepIntEventos"),
    VENTAS("ssmnuRepIntVentas"),
    ACCESOS_RECEPCION("ssmnuRepIntAccesosRecepcion");

    private final String clave;

    ClavesModulosReportes(String clave) {
        this.clave = clave;
    }

    public String getClave() {
        return clave;
    }
}
