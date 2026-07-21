package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record DetalleConsumoReferidoResponse(
        String membresiaReferidor,
        String membresiaReferido,
        Integer consecutivoReferido,
        Integer numConsumo,
        LocalDateTime fechaRegistroConsumo,
        LocalDateTime fechaGeneracionMovimiento,
        String descripcion,
        BigDecimal cargo,
        BigDecimal abono,
        String usuarioRegistro,
        Integer idMovimiento
) {
}
