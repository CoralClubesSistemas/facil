package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record MembresiaAccesoEntradaSalidaDto(
        String membresia,
        Integer numeroBeneficiario,
        Integer idDesarrolloAcceso,
        LocalDateTime fechaAcceso,
        String tipoAcceso,
        String usuarioRegistro,
        LocalDateTime fechaRegistro
) {
}
