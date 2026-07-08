package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;

@Builder
public record BeneficiarioPdfItemDto(
        String id,
        String nombre,
        String fechaNacimiento,
        String estadoCivil,
        String parentesco
) {
}
