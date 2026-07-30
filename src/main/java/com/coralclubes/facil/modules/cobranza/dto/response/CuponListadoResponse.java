package com.coralclubes.facil.modules.cobranza.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CuponListadoResponse(
        Integer id,
        String nombre,
        Integer anio,
        String descripcion,
        String origen,
        LocalDateTime inicioVigencia,
        LocalDateTime finVigencia,
        Boolean esTransferible,
        String nomenclatura,
        Integer idDesarrollo,
        String desarrollo,
        String imagen
) {}
