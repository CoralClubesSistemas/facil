package com.coralclubes.facil.modules.prospectos.dto.domain;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Datos específicos de compra cuando el prospecto asiste a la cita y adquiere una membresía.
 */
@Builder
public record DatosCompraProspecto(
        BigDecimal montoCompra,
        String membresia,
        String nombreTitularContrato,
        String tipoMembresia,
        BigDecimal montoEnganche
) {
}
