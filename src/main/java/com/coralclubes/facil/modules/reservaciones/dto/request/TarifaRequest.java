package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TarifaRequest(
        @NotNull(message = "El desarrollo es obligatorio") Integer idDesarrollo,
        @NotNull(message = "El tipo de habitación es obligatorio") Integer idTipoHabitacion,
        @NotNull(message = "El tipo de acceso es obligatorio") Integer idTipoAcceso,
        @NotNull(message = "La temporada es obligatoria") Integer idTemporada,
        @NotNull(message = "El tipo de tarifa es obligatorio") Integer idTipoTarifa,
        @NotNull(message = "El tipo de cálculo es obligatorio") Integer idTipoCalculo,
        @NotNull(message = "El año de vigencia es obligatorio") Integer anioVigencia,
        @NotNull(message = "El costo por noche es obligatorio") BigDecimal costoNoche,
        @NotNull(message = "Los puntos son obligatorios") Integer puntos,
        @NotNull(message = "El costo de persona extra es obligatorio") BigDecimal costoPersonaExtra,
        @NotNull(message = "El incremento de invitados es obligatorio") Integer incrementoInvitados
) {
}