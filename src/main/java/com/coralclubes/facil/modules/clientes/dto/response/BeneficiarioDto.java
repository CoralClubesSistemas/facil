package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record BeneficiarioDto(
        Integer numeroBeneficiario,
        String nombreCompleto,
        LocalDateTime fechaNacimiento,
        LocalDateTime fechaRegistro,
        String correoElectronico,
        String genero,
        String parentesco,
        String tipoCliente,
        String estatusCliente,
        String estadoCivil
) {
}
