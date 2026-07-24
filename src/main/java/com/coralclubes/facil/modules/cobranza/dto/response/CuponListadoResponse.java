package com.coralclubes.facil.modules.cobranza.dto.response;

import java.time.LocalDateTime;

public record CuponListadoResponse(
        Integer id,
        String nombre,
        Integer anio,
        String descripcion,
        String origen,
        String destino,
        LocalDateTime inicioVigencia,
        LocalDateTime finVigencia,
        Boolean esTransferible,
        String nomenclatura,
        Integer idDesarrollo,
        String desarrollo,
        String imagen
) {}
