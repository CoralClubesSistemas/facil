package com.coralclubes.facil.modules.reservaciones.dto.response;

import java.time.LocalDateTime;

public record TareaDashboardDto(
        Integer idTarea,
        Integer idUnidadFisica,
        String numeroHabitacion,
        String tipoHabitacion,
        Integer cantidadPersonas,
        String peticionEspecial,
        Integer idCamarista,
        String nombreCamarista,
        String claveEstatus,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaInicioLimpieza
) {}