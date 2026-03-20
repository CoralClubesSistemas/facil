package com.coralclubes.facil.modules.reservaciones.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UnidadBloqueadaDto(
        Integer idUnidadFisica,
        String numeroUnidad,
        String desarrollo,
        String tipoUnidad,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String razonBloqueo,
        String usuarioBloqueo,
        LocalDateTime fechaRegistro,
        String comentarioLargo
) {}