package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;

@Builder
public record MembresiaReferidoDto(
        Integer consecutivoReferido,
        String apellidoPaterno,
        String apellidoMaterno,
        String nombre,
        String segundoNombre,
        String nombreCompleto,
        String genero,
        String parentesco,
        String emailPrincipal,
        String emailAlterno,
        String telefono,
        String tipoCliente,
        String membresia
) {
}
