package com.coralclubes.facil.shared.events.dto;

import com.coralclubes.facil.modules.cobranza.dto.projection.MovimientoAfectadoCancelacionDto;

import java.util.List;

/**
 * Evento de dominio disparado cuando un recibo es cancelado exitosamente
 * en el módulo de cobranza.
 */
public record ReciboCanceladoEvent(
        String membresia,
        String usuario,
        List<MovimientoAfectadoCancelacionDto> movimientosAfectados
) {}