package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RegistarEvidenciaReciboCancelado (

        @NotBlank(message = "La membresía es obligatoria.")
        String numeroMembresia,

        @NotNull(message = "El número de recibo es obligatorio.")
        Integer numeroRecibo,

        @NotNull(message = "El ID de la serie de recibo es obligatorio.")
        Integer idSerieRecibo,

        @NotEmpty(message = "Debe enviar al menos un archivo de evidencia.")
        List<ArchivosEvidencia> jsonFiles
) {

    public record ArchivosEvidencia(
            @NotBlank(message = "El UUID del archivo es obligatorio.")
            String uuid,

            @NotBlank(message = "El nombre del archivo es obligatorio.")
            String fileName
    ) {
    }
}
