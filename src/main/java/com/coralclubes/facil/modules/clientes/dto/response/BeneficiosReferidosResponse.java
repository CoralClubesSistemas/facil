package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record BeneficiosReferidosResponse(
        String membresiaReferidor,
        String nombreSocioReferidor,
        Integer consecutivoReferido,
        String membresiaReferido,
        String nombreSocioReferido,
        Integer totalReferidos,
        String estatusReferidos,
        BigDecimal porcentajeDescuentoAplicar,
        BigDecimal montoDescuentoDinero,
        BigDecimal montoConsumido,
        Integer puntosPorDescontar,
        Integer puntosDescontados,
        String usuarioRegistroBeneficio,
        LocalDateTime fechaRegistroBeneficio,
        String usuarioRegistroReferido,
        LocalDateTime fechaRegistroReferido
) {
}
