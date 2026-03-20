package com.coralclubes.facil.modules.reservaciones.dto.response;

import java.time.LocalDateTime;

public record TransferenciaHabitacionDto(
        String unidadAnterior,
        String unidadNueva,
        String usuarioTransfiere,
        LocalDateTime fechaTransferencia,
        String observaciones
) {}