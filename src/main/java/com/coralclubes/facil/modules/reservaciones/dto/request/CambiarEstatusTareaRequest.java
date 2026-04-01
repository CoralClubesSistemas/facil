package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CambiarEstatusTareaRequest(
        @NotBlank(message = "El nuevo estatus es obligatorio")
        String nuevoEstatus,

        // Puede ser nulo si solo le da a "Iniciar" la propia camarista,
        // pero se llena si la Master está asignando (Estatus ASIGNADA)
        Integer idCamarista
) {}