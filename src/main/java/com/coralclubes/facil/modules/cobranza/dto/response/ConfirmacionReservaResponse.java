package com.coralclubes.facil.modules.cobranza.dto.response;

import java.util.List;
import java.util.UUID;

public record ConfirmacionReservaResponse (
        List<Integer> folios,
        UUID ordenCobranza
) {}
