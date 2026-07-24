package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record GuardarCuponRequest(
        Integer id,
        @NotBlank @Size(max = 200) String nombre,
        @NotNull Integer year,
        String descripcion,
        @NotNull Integer origen,
        @NotNull Integer destino,
        @NotNull LocalDateTime inicioVigencia,
        @NotNull LocalDateTime finVigencia,
        @NotNull Boolean esTransferible,
        @NotBlank @Size(max = 100) String nomenclatura,
        @NotNull Integer desarrollo,
        ConfiguracionMembresias configuracionMembresias,
        List<CondicionesBeneficios> condiciones,
        List<CondicionesBeneficios> beneficios
) {
    public record Periodos(
            @NotNull String id,
            String nombre,
            @NotNull LocalDateTime fechaInicio,
            @NotNull LocalDateTime fechaFin
    ) {}

    public record Membresias(
            @NotNull Integer idTipoMembresia,
            Map<String, Integer> cantidades
    ) {}

    public record ConfiguracionMembresias(
            List<Periodos> periodos,
            List<Membresias> membresias
    ) {}

    public record CondicionesBeneficios(
            @NotNull String clave,
            String tipo,
            @NotNull String valor
    ) {}
}
