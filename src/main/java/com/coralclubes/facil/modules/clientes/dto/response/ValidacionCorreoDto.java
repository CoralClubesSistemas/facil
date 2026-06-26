package com.coralclubes.facil.modules.clientes.dto.response;

public record ValidacionCorreoDto(
        boolean correoEmpleado,
        boolean correoClienteSinRegistro,
        boolean correoClienteConRegistro
) {}
