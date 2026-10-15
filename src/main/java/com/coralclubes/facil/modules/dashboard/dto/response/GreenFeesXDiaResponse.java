package com.coralclubes.facil.modules.dashboard.dto.response;


public record GreenFeesXDiaResponse(
    Long id,
    String categoria,
    String membresia,
    String cliente,
    String fechaVenta,
    String cantidad,
    String total
) {}
