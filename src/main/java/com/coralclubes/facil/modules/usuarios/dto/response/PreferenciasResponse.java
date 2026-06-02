package com.coralclubes.facil.modules.usuarios.dto.response;

import java.util.Map;

public record PreferenciasResponse(
    Map<String, Object> preferencias
) {}
