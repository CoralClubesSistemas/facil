package com.coralclubes.facil.modules.usuarios.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record ActualizarPreferenciasRequest(
    @NotNull(message = "Las preferencias no pueden ser nulas")
    Map<String, Object> preferencias
) {}
