package com.coralclubes.facil.modules.reportes.dto.response;

import lombok.Builder;

@Builder
public record ParametroMapeoDto(
        Integer posicion,
        String rol,
        String nombreJava,
        String nombreSP,
        String tipoDato,
        Integer longitud,
        Boolean esObligatorio
) {}
