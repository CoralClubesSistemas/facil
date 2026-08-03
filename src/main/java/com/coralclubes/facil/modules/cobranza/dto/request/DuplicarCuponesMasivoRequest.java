package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DuplicarCuponesMasivoRequest(
        @NotEmpty List<Integer> ids,
        @NotNull Integer targetYear
) {}
