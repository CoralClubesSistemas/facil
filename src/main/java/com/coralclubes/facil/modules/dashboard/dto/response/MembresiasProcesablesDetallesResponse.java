package com.coralclubes.facil.modules.dashboard.dto.response;

import java.math.BigDecimal;

public record MembresiasProcesablesDetallesResponse(
    String membresia,
    String tipoMembresia,
    String nombreCliente,
    String fechaVenta,
    String procedencia,
    BigDecimal importeVenta,
    BigDecimal enganchePactado,
    BigDecimal montoProcesable,
    String fechaProcesable,
    BigDecimal montoPagado,
    Integer numeroMensualidades,
    String estatusVenta
) {}
