package com.coralclubes.facil.modules.clientes.dto.request;

import java.time.LocalDateTime;
import java.util.List;

public record FiltroCuponesMembresiaRequest(
        String membresia,
        Integer year,
        LocalDateTime fechaInicioConsulta,
        LocalDateTime fechaFinConsulta,
        String origenCupon,
        Integer desarrolloConsumo,
        List<String> objetivos
) {
    public String getObjetivosFormateados() {
        return (objetivos != null && !objetivos.isEmpty())
                ? String.join(",", objetivos)
                : null;
    }
}
