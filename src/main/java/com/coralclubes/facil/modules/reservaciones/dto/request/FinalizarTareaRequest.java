package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record FinalizarTareaRequest(
        @NotNull(message = "Debe indicar el almacén/carrito de donde sacó los insumos")
        Integer idAlmacenOrigen,

        @NotNull(message = "La lista de consumos no puede ser nula")
        List<ConsumoRealDto> consumos
) {}