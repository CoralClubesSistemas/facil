package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record ConsumoPuntosDto(
        Integer numeroBeneficiario,
        String nombreBeneficiario,
        Integer consecutivoConsumo,
        Integer idDesarrolloConsumo,
        String desarrolloConsumo,
        String tipoClienteAcceso,
        String tipoAccesoDesarrollo,
        String periodoUsoDesarrollo,
        Integer puntosHospedaje,
        Integer puntosInstalaciones,
        Integer puntosCampoGolf,
        String numeroAutorizacion,
        LocalDateTime fechaConsumo,
        String descripcionMovimiento
) {
}
