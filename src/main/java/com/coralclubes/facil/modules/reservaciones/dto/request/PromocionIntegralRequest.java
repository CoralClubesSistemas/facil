package com.coralclubes.facil.modules.reservaciones.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PromocionIntegralRequest(
        Integer idPromocion, // Nulo para crear, con valor para editar
        @NotBlank String nombre,
        String descripcion,
        @NotBlank String codigo,
        @NotNull Integer stockTotal,
        Integer stockAdicional, // Para cuando se edita y se quiere sumar stock
        @NotNull LocalDateTime fechaInicio,
        @NotNull LocalDateTime fechaFin,
        @NotNull LocalDateTime fechaVisible,
        Boolean esActiva,
        Boolean esPrivada,
        Boolean esGlobal,
        String uuidImagen, // ID de la imagen asociada (opcional)
        @Valid List<BeneficioRequest> beneficios,
        @Valid List<ReglaRequest> reglas
) {
    @Builder
    public record BeneficioRequest(
            @NotBlank String tipoBeneficioClave, // Ej: "DESC_MONEY"
            @NotNull BigDecimal valor,
            @NotBlank String objetivoClave // Ej: "TOTAL" o "PROX_RESERV"
    ) {}

    @Builder
    public record ReglaRequest(
            @NotBlank String tipoReglaClave, // Ej: "HOTEL"
            @NotBlank String comparadorClave, // Ej: "IN" o ">="
            @Valid List<ReglaDetalleRequest> detalles
    ) {}

    @Builder
    public record ReglaDetalleRequest(
            Integer valorCatalogoId, // Para IDs (Ej. ID de Hotel)
            BigDecimal valorNumerico, // Para montos exactos
            BigDecimal valorSecundario // Para rangos (BETWEEN)
    ) {}
}