package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.util.List;

@Builder
public record DatosReporteBeneficiariosDto(
        String razonSocial,
        String slogan,
        String fechaEmision,
        String membresia,
        String clasificacionMembresia,
        String tipoMembresia,
        String desarrollo,
        String direccionMembresia,
        List<BeneficiarioPdfItemDto> beneficiarios
) {
}
