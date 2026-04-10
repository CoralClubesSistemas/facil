package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

@Builder
public record ProcesarPagoResponse(
        Integer intentoPagoId,
        String estatus, // 'APROBADO', 'PENDIENTE'
        String mensajeAccion, // Ej. "Deslice tarjeta en Terminal", "Cobro exitoso"
        String urlPago // Opcional, solo si es link
) {}