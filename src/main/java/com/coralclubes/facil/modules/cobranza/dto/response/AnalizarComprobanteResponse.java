package com.coralclubes.facil.modules.cobranza.dto.response;

import com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.dto.MotorAnalisis;

public record AnalizarComprobanteResponse(
        ComprobantePagoAnalizadoDto comprobante,
        MotorAnalisis motorUsado,
        String mensaje
) {}
