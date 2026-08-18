package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneracionMovimientoRequest {
    @NotBlank
    private String membresia;

    @NotNull
    private Integer tipoMovimientoId;

    // Campos comunes
    private LocalDate fechaVencimiento;
    private Integer desarrolloConsumo;

    // Campos dinámicos para casos especiales (anios, incluirPrevios, cantidadMovimientos, descripcion, cuota, etc.)
    @Builder.Default
    private Map<String, Object> parametrosEspeciales = new HashMap<>();
}
