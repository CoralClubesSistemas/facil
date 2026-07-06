package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record MembresiaDatosDto(
        String membresia,
        Integer idDesarrollo,
        String desarrollo,
        Integer idEstatusMembresia,
        String estatusMembresia,
        Integer idPuntoDeVenta,
        String puntoDeVenta,
        Integer idTipoMembresia,
        String tipoMembresia,
        Integer idClasificacionMembresia,
        String clasificacionMembresia,

        // Plan de venta info
        LocalDateTime fechaVenta,
        Integer numeroPlan,
        BigDecimal precioPlan,
        BigDecimal descuento,
        BigDecimal montoNeto,
        BigDecimal enganche,
        BigDecimal intereses,
        BigDecimal saldo,
        Integer numeroMensualidades,
        BigDecimal importeMensualidades,
        LocalDateTime inicioMensualidades,

        // Procesable info
        BigDecimal montoProcesable,
        Integer estatusProcesable,
        String descripcionEstatusProcesable,
        LocalDateTime fechaProcesable
) {
}
