package com.coralclubes.facil.modules.clientes.dto.projection;

import lombok.Builder;

@Builder
public record ClienteLoginResult(
        String idUsuario,
        String membresia,
        String correo,
        String tokenProveedor,
        String passwordHash
) {}
