package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;

@Builder
public record MembresiaTarjetaDto(
        String tipoFranquicia,
        String tarjeta,
        Integer idInstrumento,
        String tipoTarjeta,
        String clabe,
        String vigencia,
        Integer idBanco,
        String banco,
        String codigoSeguridad,
        Integer idPrioridad,
        String prioridad,
        Boolean esTitularDiferente,
        String nombreTitular,
        Boolean exentarCargoAutomatico,
        Integer idEstatus,
        String estatusTarjeta,
        String usuarioRegistro,
        String fechaActualizacion
) {
}
