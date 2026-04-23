package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record InformacionSocio (
    String membresia,
    String nombreCompleto,
    String nombre,
    String segundoNombre,
    String apellidoPaterno,
    String apellidoMaterno,
    String correo,
    String correoAlternativo,
    String telefono,
    String telefonoAlternativo,
    LocalDate fechaNacimiento,
    int tipoMembresiaId,
    String tipoMembresia,
    int clasificacionMembresiaId,
    String clasificacionMembresia,
    int desarrolloId,
    String desarrollo,
    int estatusMembresiaId,
    String estatusMembresia,
    int carteraCobranzaId,
    String carteraCobranza,
    Integer vigenciaOriginal,
    String vigenciaRestante,
    String convenioCie
){}
