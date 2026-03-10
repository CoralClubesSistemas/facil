package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenCheckoutResponse {

    private List<ItemCheckoutDto> habitaciones;
    private ResumenFinancieroDto resumen;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemCheckoutDto {
        private Integer rrtId;
        private Integer idTipoHabitacion;
        private String nombreHabitacion;
        private Integer cantidad;
        private BigDecimal costoUnitario;
        private BigDecimal subtotalHabitacion;
        private BigDecimal descuentoAplicado;
        private String motivoDescuento;
        private BigDecimal totalFinalHabitacion;
        private OpcionPagoPuntosDto opcionPagoPuntos;
        private List<NocheCheckoutDto> desgloseNoches;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NocheCheckoutDto {
        private String fecha;
        private BigDecimal costo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenFinancieroDto {
        private BigDecimal subtotalOriginal;
        private BigDecimal totalDescuentos;
        private BigDecimal baseGravable;
        private BigDecimal iva;
        private BigDecimal ish;
        private BigDecimal totalAPagar;
        private boolean cuponValido;
        private String mensajeCupon;
    }
}