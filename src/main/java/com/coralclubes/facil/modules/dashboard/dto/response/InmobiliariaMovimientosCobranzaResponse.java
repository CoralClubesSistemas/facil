package com.coralclubes.facil.modules.dashboard.dto.response;

import java.math.BigDecimal;

public record InmobiliariaMovimientosCobranzaResponse(
    Long id,
    String membresia,
    String cliente,
    String fecha,
    String clasificacionMembresia,
    String seccion,
    String fraccionamiento,
    String manzana,
    String lote,
    BigDecimal area,
    BigDecimal precioMetro,
    BigDecimal total
) {}
