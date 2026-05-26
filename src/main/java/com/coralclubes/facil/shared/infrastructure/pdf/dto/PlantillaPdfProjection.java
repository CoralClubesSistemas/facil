package com.coralclubes.facil.shared.infrastructure.pdf.dto;

import java.io.Serializable;

/* DTO en el que mapearemos la respuesta del sp que obtiene las plantillas de pdf de la base de datos */
public record PlantillaPdfProjection(
    Integer id,
    String codigo,
    String contenido
) implements Serializable {} // implementamos Serializable para que pueda ser almacenado en caché sin problemas
