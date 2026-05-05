package com.coralclubes.facil.modules.clientes.dto.response;

import java.time.LocalDateTime;

public record NotasClienteResponse(
        String campoAlarma,
        String membresia,
        Integer consecutivo,
        LocalDateTime fechaNota,
        Integer clasificacionNotaId,
        String clasificacion,
        String usuarioRegistra,
        String nota,
        LocalDateTime fechaFinAlerta,
        String usuarioDesactivaAlerta,
        String respondio,
        String telefono,
        String extension,
        String tipoTelefono
) {
}

