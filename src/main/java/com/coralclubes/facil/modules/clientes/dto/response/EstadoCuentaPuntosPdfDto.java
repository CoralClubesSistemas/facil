package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.util.List;

@Builder
public record EstadoCuentaPuntosPdfDto(
        String razonSocial,
        String slogan,
        String periodoInicio,
        String periodoFin,
        String fechaEmision,
        String titular,
        String membresia,
        String tipoMembresia,
        String desarrollo,
        String clasificacionMembresia,
        String domicilioSocio,
        List<EstadoCuentaPuntosPdfItemDto> movimientosGeneralPuntos,
        EstadoCuentaPuntosPdfTotalesDto totalesGenerales
) {
}
