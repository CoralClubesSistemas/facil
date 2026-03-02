package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record TarifaDto(
        Integer idTarifa,
        Integer idDesarrollo,
        String nombreDesarrollo,
        Integer idTipoHabitacion,
        String nombreTipoHabitacion,
        Integer idTipoAcceso,
        String nombreTipoAcceso,
        Integer idTipoTemporada,
        String nombreTemporada,
        Integer idTipoTarifa,
        String nombreTipoTarifa,
        Integer idTipoCalculo,
        String nombreTipoCalculo,
        Integer capacidad,
        String tipoUnidadLegacy,
        Integer anioVigencia,
        BigDecimal costoNoche,
        Integer puntos,
        BigDecimal costoPersonaExtra,
        Integer incrementoInvitados
) {}