package com.coralclubes.facil.modules.cobranza.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record CuponDetalleResponse(
        Integer id,
        String nombre,
        Integer year,
        String descripcion,
        Integer origen,
        Integer destino,
        LocalDateTime inicioVigencia,
        LocalDateTime finVigencia,
        Boolean esTransferible,
        String nomenclatura,
        Integer desarrollo,
        ConfiguracionMembresiasDto configuracionMembresias,
        List<AtributoCuponDto> condiciones,
        List<AtributoCuponDto> beneficios
) {
    // DTOs auxiliares (puedes reutilizar los del request si ya los tienes creados)
    public record ConfiguracionMembresiasDto(
            List<PeriodoDto> periodos,
            List<MembresiaCantidadesDto>
            membresias) {}

    public record PeriodoDto(
            String id,
            String nombre,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {}

    public record MembresiaCantidadesDto(
            Integer idTipoMembresia,
            Map<String, Integer> cantidades) {}

    public record AtributoCuponDto(
            String clave,
            String nombre,
            String tipo,
            String valor) {}
}