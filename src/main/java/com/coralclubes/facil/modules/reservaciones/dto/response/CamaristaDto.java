package com.coralclubes.facil.modules.reservaciones.dto.response;

public record CamaristaDto(
        Integer idCamarista,
        String nombre,
        Integer idDesarrollo,
        Boolean activo
) {}