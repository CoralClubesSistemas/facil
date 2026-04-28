package com.coralclubes.facil.shared.enums;

public enum MovimientosEnum {
    RESERVACIONES(22);

    private final int id;

    MovimientosEnum(int id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
