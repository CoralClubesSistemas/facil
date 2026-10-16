package com.coralclubes.facil.shared.domain.enums;

public enum DesarrollosEnum {
    CC(1, "CORAL CUERNAVACA"),
    SM(2, "SANTA MARIA"),
    CG(6, "CORAL GOLF");

    private final int id;
    private final String nombre;

    DesarrollosEnum(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public static DesarrollosEnum fromId(int id) {
        for (DesarrollosEnum desarrollo : values()) {
            if (desarrollo.id == id) {
                return desarrollo;
            }
        }
        throw new IllegalArgumentException("Desarrollo con ID " + id + " no encontrado.");
    }
}