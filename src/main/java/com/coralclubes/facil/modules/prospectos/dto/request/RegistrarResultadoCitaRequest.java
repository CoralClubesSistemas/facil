package com.coralclubes.facil.modules.prospectos.dto.request;

import com.coralclubes.facil.modules.prospectos.dto.domain.DatosCompraProspecto;
import com.coralclubes.facil.modules.prospectos.dto.domain.ResultadoCita;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * Request DTO para registrar el resultado de una cita de prospecto desde sistemas externos o internos.
 */
@Builder
public record RegistrarResultadoCitaRequest(
        @NotBlank(message = "El ID externo del prospecto es obligatorio")
        String idExterno,

        Integer prospectoId,

        Integer idCita,

        @NotNull(message = "El resultado de la cita es obligatorio")
        ResultadoCita resultado,

        String desarrollo,

        String observaciones,

        String usuario,

        DatosCompraProspecto datosCompra
) {
}
