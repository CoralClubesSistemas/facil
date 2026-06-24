package com.coralclubes.facil.modules.clientes.dto.projection;

import lombok.Builder;

@Builder
public record ClienteValidacionMembresiaResult(
        String membresia,
        String nombreCompleto,
        Integer desarrollo,
        String descripcionDesarrollo,
        Integer estatus,
        String descripcionEstatus,
        String correoPersonal,
        String correoTrabajo,
        String tipoMembresia,
        Integer clasificacionMembresia,
        String descripcionClasificacion,
        String claveClasificacion,
        Boolean registroUserSystem
) {}
