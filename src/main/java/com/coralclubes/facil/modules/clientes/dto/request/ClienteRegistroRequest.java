package com.coralclubes.facil.modules.clientes.dto.request;

import lombok.Builder;

@Builder
public record ClienteRegistroRequest(
        String membresia,
        String correo,
        String password,
        String tokenProveedor
) {}
