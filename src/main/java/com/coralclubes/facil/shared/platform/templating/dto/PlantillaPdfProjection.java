package com.coralclubes.facil.shared.platform.templating.dto;

public record PlantillaPdfProjection(
        Integer id,
        String codigo,
        String nombre,
        String descripcion,
        String contenido,
        Boolean activo
) {
}
