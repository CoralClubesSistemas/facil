package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record SimularCalculoDescuentoRequest(
        @NotEmpty List<@Valid GenerarOrdenCobranzaMovimientoRequest> movimientos,
        List<BigDecimal> porcentajesDescuentoCascada,
        Integer idDesarrollo,
        Integer clasificacionMembresia,
        String usuarioAutoriza,
        Boolean agregarIva,
        Boolean ivaIncluido
) {
}
