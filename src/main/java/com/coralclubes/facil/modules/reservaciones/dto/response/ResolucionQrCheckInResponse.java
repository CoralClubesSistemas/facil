package com.coralclubes.facil.modules.reservaciones.dto.response;

import com.coralclubes.facil.shared.platform.qr.enums.SystemQrStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record ResolucionQrCheckInResponse(
        String token,
        String membresia,
        SystemQrStatus status,
        boolean consumible,
        Integer maxUses,
        Integer usedCount,
        Integer usosDisponibles,
        String mensaje,
        List<DetalleReservacionDto> reservaciones
) {}
