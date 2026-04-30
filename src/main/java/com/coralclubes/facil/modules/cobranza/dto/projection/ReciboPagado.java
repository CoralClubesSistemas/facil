package com.coralclubes.facil.modules.cobranza.dto.projection;

import java.math.BigDecimal;
import java.util.List;

public record ReciboPagado(
        String membresia,
        Integer numeroRecibo,
        Integer serieReciboId,
        Integer tipoMembresia,
        Integer clasificacionMembresia,
        String usuario,
        Integer desarrolloId,
        BigDecimal totalPagado,
        List<MovimientosReciboPagado> movimientosAfectados
) {}
