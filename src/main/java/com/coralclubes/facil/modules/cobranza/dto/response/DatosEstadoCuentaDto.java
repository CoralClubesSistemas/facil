package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record DatosEstadoCuentaDto(
        String razonSocial,
        String slogan,
        String periodoInicio,
        String periodoFin,
        String fechaEmision,
        String fechaLimitePago,
        String titular,
        String membresia,
        String tipoMembresia,
        String telefonoContacto,
        String correoContacto,
        String domicilioSocio,
        List<MovimientoEstadoCuentaDto> movimientos,
        ResumenTotalesEstadoCuentaDto resumenTotales
) {
}
