package com.coralclubes.facil.modules.clientes.dto.response;

import com.coralclubes.facil.modules.clientes.dto.projection.CuponMembresiaDb;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponBeneficioResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponCondicionResponse;

import java.util.List;

public record CuponMembresiaCompletoResponse(
        CuponMembresiaDb cupon,
        List<CuponBeneficioResponse> beneficios,
        List<CuponCondicionResponse> condiciones
) {
}
