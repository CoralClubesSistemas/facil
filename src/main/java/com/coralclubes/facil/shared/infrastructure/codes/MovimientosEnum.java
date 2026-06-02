package com.coralclubes.facil.shared.infrastructure.codes;

// enum en el que definimos los ids de los movimientos que posiblemente se puedan usar en el sistema, para evitar hardcodearlos.
public enum MovimientosEnum {
    RESERVACIONES(22),
    PAGO(4),
    BONIFICACION(54),
    ENGANCHE(1),
    MENSUALIDAD(2),
    INTERESES(6),
    DESCUENTO(8);

    private final int id;

    MovimientosEnum(int id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
