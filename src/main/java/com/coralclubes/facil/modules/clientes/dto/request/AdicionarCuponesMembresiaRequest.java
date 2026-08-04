package com.coralclubes.facil.modules.clientes.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdicionarCuponesMembresiaRequest(
        @NotNull(message = "El id (PQAC_ID) es obligatorio")
        Integer id,
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad de cupones a adicionar debe ser mayor a cero")
        Integer cantidad
) {
}
