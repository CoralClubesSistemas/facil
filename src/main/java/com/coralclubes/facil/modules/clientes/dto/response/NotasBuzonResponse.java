package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record NotasBuzonResponse(
        String numeroCaso,
        String membresia,
        Integer consecutivoPadre,
        Integer consecutivoHijo,
        String desarrollo,
        String nombreCliente,
        String nota,
        LocalDateTime fechaNota,
        LocalDateTime fechaRegistro,
        String clasificacionNota,
        String clasificacionNotaBuzon,
        String estatus,
        String correoElectronico,
        String telefono1,
        String telefono2,
        String usuarioRegistra
) {
}
