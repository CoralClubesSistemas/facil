package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record InformacionSocioBusqueda (
        String membresia,
        String nombreCompleto,
        String correo,
        String telefono,
        String tipoMembresia,
        String clasificacionMembresia,
        String desarrollo,
        String estatusMembresia,
        String carteraCobranza
){
}
