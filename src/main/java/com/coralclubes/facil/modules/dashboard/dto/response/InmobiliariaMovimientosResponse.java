package com.coralclubes.facil.modules.dashboard.dto.response;


public record InmobiliariaMovimientosResponse(
    Long id,
    String membresia,
    String cliente,
    String fecha,
    String clasificacionMembresia,
    String seccion,
    String fraccionamiento,
    String manzana,
    String lote,
    String area,
    String precioMetro,
    String total,
    String enganche,
    String montoProcesable,
    String fechaProcesable,
    String montoPagado
) {}
