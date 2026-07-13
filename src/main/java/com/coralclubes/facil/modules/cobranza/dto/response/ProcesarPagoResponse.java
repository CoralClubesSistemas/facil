package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.util.Map;

@Builder
public record ProcesarPagoResponse(
        Integer intentoPagoId,
        String estatus, // 'APROBADO', 'PENDIENTE'
        String mensajeAccion, // Ej. "Deslice tarjeta en Terminal", "Cobro exitoso"
        Map<String, Object> datosAdicionales
) {}