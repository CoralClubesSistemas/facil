package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record EliminarTarifasRequest(
        @NotEmpty(message = "Debe enviar al menos un ID para eliminar")
        List<Integer> ids
) {}