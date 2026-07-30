package com.coralclubes.facil.modules.clientes.dto.response;

import java.time.LocalDateTime;

public record CuponMembresiaDetalleResponse(
        Integer consecutivo,
        Integer numeroOrden,
        String folioDescripcion,
        String estatus,
        Integer estatusId,
        LocalDateTime fechaOtorgado,
        LocalDateTime fechaDescuento,
        String usuarioOtorga,
        String usuarioDescuenta,
        Integer cuponId,
        String nombreCupon
) {}
