package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TransferirUnidadRequest(
        @NotBlank(message = "La membresía es obligatoria")
        String membresia,

        @NotNull(message = "El consecutivo es obligatorio")
        Integer consecutivo,

        @NotNull(message = "El nuevo tipo de unidad (Lógica) es obligatorio")
        Integer nuevoRhdtId,

        @NotNull(message = "La nueva habitación física es obligatoria")
        Integer nuevoRunId,

        @NotNull(message = "El importe no puede ser nulo")
        @DecimalMin(value = "0.0", message = "El importe no puede ser negativo")
        BigDecimal importeDiferencia,

        @Size(max = 250, message = "Las observaciones no pueden exceder los 250 caracteres")
        String observaciones,

        Boolean bloquearUnidadAnterior
) {}