package com.coralclubes.facil.modules.clientes.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AsignarCuponesMembresiaRequest(
        @NotBlank String membresia,
        @NotBlank String origen,
        @NotEmpty List<@Valid CuponAsignacionItem> cupones
) {
    public record CuponAsignacionItem(
            @NotNull Integer cuponId,
            @NotNull Integer cantidad
    ) {}
}
