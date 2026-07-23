package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record GuardarCuponRequest(
        Integer id,
        @NotBlank @Size(max = 50) String nombre,
        @NotBlank @Size(max = 200) String descripcion,
        @NotNull Integer origene,
        @NotNull Integer destino,
        @NotNull LocalDateTime inicioVigencia,
        @NotNull LocalDateTime finVigencia,
        @NotNull Boolean esTransferible,
        @NotBlank @Size(max = 200) String formato,
        @NotBlank @Size(max = 100) String nomenclatura,
        String beneficios,
        String condiciones
) {}
