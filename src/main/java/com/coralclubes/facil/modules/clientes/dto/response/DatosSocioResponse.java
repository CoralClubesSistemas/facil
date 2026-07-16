package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record DatosSocioResponse(
        String membresia,
        LocalDate fechaNacimiento,
        Integer edad,
        String rfc,
        String curp,
        String genero,
        String estadoCivil,
        String ocupacion,
        String estatusCliente,
        String mailPersonal,
        String mailTrabajo,
        LocalDateTime fechaRegistro,
        String nombreCompleto,
        String nombre,
        String segundoNombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String nombreTitularAdicional,
        List<DomicilioSocioDto> domicilios
) {}
