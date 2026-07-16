package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;

@Builder
public record DomicilioSocioDto(
        String direccion,
        String tipoDomicilio
) {}
