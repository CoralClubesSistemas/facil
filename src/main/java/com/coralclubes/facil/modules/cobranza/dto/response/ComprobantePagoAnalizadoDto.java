package com.coralclubes.facil.modules.cobranza.dto.response;

import java.math.BigDecimal;

public record ComprobantePagoAnalizadoDto(
        String bancoEmisor,
        String bancoReceptor,
        BigDecimal monto,
        String fechaOperacion,
        String horaOperacion,
        String claveRastreo,
        String referencia,
        String cuentaOrdenante,
        String cuentaBeneficiaria,
        String beneficiario,
        String ordenante,
        String concepto,
        String tipoOperacion
) {}
