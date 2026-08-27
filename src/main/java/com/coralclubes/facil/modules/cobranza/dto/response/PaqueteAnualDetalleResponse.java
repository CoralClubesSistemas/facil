package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PaqueteAnualDetalleResponse(
        Integer id,
        Integer desarrolloId,
        String desarrollo,
        Integer anio,
        Integer tipoMembresiaId,
        String tipoMembresia,
        Integer clasificacionMembresiaId,
        String clasificacionMembresia,
        Boolean activo,
        LocalDateTime fechaRegistro,
        String usuarioRegistro,
        List<PaqueteAnualDescuentoResponse> configuracionDescuentos,
        List<PaqueteAnualMovimientoResponse> movimientos
) {}
