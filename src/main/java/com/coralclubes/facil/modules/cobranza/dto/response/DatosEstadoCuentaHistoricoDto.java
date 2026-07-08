package com.coralclubes.facil.modules.cobranza.dto.response;

import lombok.Builder;
import java.util.List;

@Builder
public record DatosEstadoCuentaHistoricoDto(
        String razonSocial,
        String slogan,
        String periodoInicio,
        String periodoFin,
        String fechaEmision,
        String titular,
        String membresia,
        String tipoMembresia,
        String telefonoContacto,
        String correoContacto,
        String domicilioSocio,
        Integer puntosLiberados,
        Integer puntosConsumidos,
        Integer puntosDisponibles,
        List<MovimientoHistoricoTreeDto> movimientosHistoricos
) {
}
