package com.coralclubes.facil.modules.usuarios.dto.response;

import lombok.Builder;

@Builder
public record DatosCorreoUsuarioDto(
        String usuario,
        String imagenFirma,
        String correoAutorizado,
        String contrasenaCorreo,
        String telefono
) {
}
