package com.coralclubes.facil.modules.dashboard.dto.response;


public record UltimasMembresiasResponse(
    String desarrollo,
    Integer totalMembresias,
    String fechaInicio,
    String fechaActual
) {}
