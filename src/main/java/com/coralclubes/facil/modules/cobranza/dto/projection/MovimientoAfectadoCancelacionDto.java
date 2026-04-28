package com.coralclubes.facil.modules.cobranza.dto.projection;

/**
 * Representa un movimiento padre afectado por la cancelación de un recibo.
 * Se utiliza para disparar reglas de negocio asíncronas (Reservaciones, Puntos, etc).
 */
public record MovimientoAfectadoCancelacionDto(
        Integer idMovimiento,
        Integer tipoMovimiento,
        Integer numeroPlan,
        String concepto
) {}
