package com.coralclubes.facil.shared.events.dto;

import com.coralclubes.facil.modules.cobranza.dto.projection.MovimientoAfectadoCancelacionDto;

import java.util.List;
import java.util.Map;

/**
 * Evento de dominio disparado cuando un recibo es cancelado exitosamente
 * en el módulo de cobranza.
 */
public record ReciboCanceladoEvent(
        String membresia,
        Integer tipoMembresia,
        Integer clasificacionMembresia,
        String usuario,
        String motivoCancelacion,
        String correoCliente,
        String correoUsuario,
        List<MovimientoAfectadoCancelacionDto> movimientosAfectados,
        Map<String, Boolean> decisionesUsuario
) {}