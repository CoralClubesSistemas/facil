package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PropuestaPaqueteAnualResponse(
        Integer propuestaId,
        Integer paqueteAnualId,
        String membresia,
        Integer anio,
        Integer totalBeneficiariosActivos,
        BigDecimal porcentajeDescuentoAplicado,
        BigDecimal subtotalGeneral,
        BigDecimal descuentoGeneral,
        BigDecimal totalGeneral,
        List<PaqueteAnualDescuentoResponse> esquemasAplicados,
        List<CotizacionPaqueteAnualMovimientoResponse> movimientos,
        List<CuponBeneficioPaqueteAnualResponse> cupones,
        LocalDateTime vigenciaPropuesta,
        LocalDateTime fechaRegistro,
        String usuarioRegistro
) {}
