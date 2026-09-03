package com.coralclubes.facil.modules.cobranza.dto.request;

import com.coralclubes.facil.modules.cobranza.dto.response.CotizacionPaqueteAnualMovimientoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponBeneficioPaqueteAnualResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.PaqueteAnualDescuentoResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GuardarPropuestaPaqueteAnualRequest(
        @NotNull(message = "El ID del paquete anual es obligatorio")
        Integer paqueteAnualId,

        @NotBlank(message = "La membresía es obligatoria")
        String membresia,

        @NotNull(message = "El año es obligatorio")
        Integer anio,

        @NotNull(message = "El total de beneficiarios activos es obligatorio")
        Integer totalBeneficiariosActivos,

        @NotNull(message = "El porcentaje de descuento aplicado es obligatorio")
        BigDecimal porcentajeDescuentoAplicado,

        @NotNull(message = "El subtotal general es obligatorio")
        BigDecimal subtotalGeneral,

        @NotNull(message = "El descuento general es obligatorio")
        BigDecimal descuentoGeneral,

        @NotNull(message = "El total general es obligatorio")
        BigDecimal totalGeneral,

        List<PaqueteAnualDescuentoResponse> esquemasAplicados,

        @NotEmpty(message = "La lista de movimientos es obligatoria")
        List<CotizacionPaqueteAnualMovimientoResponse> movimientos,

        List<CuponBeneficioPaqueteAnualResponse> cupones,

        LocalDate fechaPrevistaCompra
) {}
