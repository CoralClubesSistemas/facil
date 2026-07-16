package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record BeneficiarioDto(
        Integer numeroBeneficiario,
        String nombreCompleto,
        LocalDateTime fechaNacimiento,
        Integer edad,
        LocalDateTime fechaRegistro,
        String correoElectronico,
        String genero,
        String parentesco,
        String tipoCliente,
        String estatusCliente,
        String estadoCivil,
        String numeroCredencial,
        String uuidCredencial,
        Integer anioVigencia,
        Integer mesVigencia,
        String mesVigenciaTexto,
        String ultimoMovimiento,
        String urlImagen
) {
}
