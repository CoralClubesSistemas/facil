package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record CheckInOutEspecialCotizacionDto(
        Boolean aplicaCheckinAnticipado,
        Integer minutosAntesCheckin,
        BigDecimal cargoCheckin,
        Boolean yaTieneCargoCheckin,
        Boolean aplicaCheckoutPosterior,
        Integer minutosDespuesCheckout,
        BigDecimal cargoCheckout,
        Boolean yaTieneCargoCheckout,
        LocalDateTime fechaEntrada,
        LocalDateTime fechaSalida,
        Integer minutosMaxCheckin,
        Integer minutosMaxCheckout
) {}
