package com.coralclubes.facil.modules.dashboard.dto.response;


public record UltimosClientesResponse(
    String membresia,
    String primerNombre,
    String segundoNombre,
    String apellidoPaterno,
    String apellidoMaterno,
    String desarrollo,
    String fecha
) {}
