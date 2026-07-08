package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record PuntosLiberadosDto(
        Integer numeroPlan,
        Integer idMovimiento,
        Integer cantidadPuntos,
        LocalDateTime fechaLiberacion,
        String conceptoLiberacion,
        String folioRecibo,
        String usuarioRegistro,
        String estatusPuntos
) {
}
