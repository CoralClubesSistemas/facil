package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record InformacionSocioPortales(
        String membresia,
        String nombreCompleto,
        String nombre,
        String segundoNombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String correo,
        String correoAlternativo,
        String telefono,
        String telefonoAlternativo,
        LocalDate fechaNacimiento,
        int desarrolloId,
        String desarrollo
) {
}
