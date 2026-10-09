package com.coralclubes.facil.shared.domain.enums;

import java.util.Optional;

public enum ClavesModulos {
    AMADELLAVES("smnuHousekeeping"),
    RESERVACIONES("mnuControlDeReservaciones"),
    RESERVACIONES_RECEPCION("smnuRecepcion");

    private final String clave;

    ClavesModulos(String clave) {
        this.clave = clave;
    }

    public String getClave() {
        return clave;
    }

    public static Optional<ClavesModulos> desdeNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(nombre.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
