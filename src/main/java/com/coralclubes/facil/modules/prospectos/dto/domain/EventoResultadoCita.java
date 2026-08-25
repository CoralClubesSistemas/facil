package com.coralclubes.facil.modules.prospectos.dto.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Evento de dominio emitido al registrar el resultado de una cita de prospecto.
 */
@Builder
public record EventoResultadoCita(
        String idExterno,
        Integer prospectoId,
        Integer idCita,
        ResultadoCita resultado,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime fechaHoraEvento,
        String desarrollo,
        String observaciones,
        DatosCompraProspecto datosCompra,
        String usuario
) {
}
