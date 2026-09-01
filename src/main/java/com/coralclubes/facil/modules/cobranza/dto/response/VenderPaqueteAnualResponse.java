package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record VenderPaqueteAnualResponse(
        Integer propuestaId,
        Integer paqueteAnualId,
        String membresia,
        Integer anio,
        Integer numeroOrden,
        UUID ordenUuid,
        Integer desarrolloId,
        BigDecimal subtotalGeneral,
        BigDecimal descuentoGeneral,
        BigDecimal totalGeneral,
        BigDecimal porcentajeDescuentoAplicado,
        List<MovimientoGeneradoPaqueteAnualDto> movimientosGenerados,
        List<PaqueteAnualDescuentoResponse> esquemasAplicados,
        List<CuponBeneficioPaqueteAnualResponse> cupones
) {}
