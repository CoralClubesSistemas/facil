package com.coralclubes.facil.modules.clientes.dto.response;

import java.math.BigDecimal;

public record CuponDisponibleDto(
        String tipoDescuento,
        Integer paqueteId,
        Integer consecutivo,
        BigDecimal porcentajeDescuento
) {}