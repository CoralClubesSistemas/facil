package com.coralclubes.facil.modules.reservaciones.model.promociones.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservacionContexto {
    private String groupId;
    private String membresia;
    private Integer idDesarrollo;
    private String desarrollo;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;
    private Integer idTipoTarifa;

    private List<ItemContexto> items;
    private BigDecimal montoTotalCarrito;
    private ItemContexto unidadElegidaParaDescuento;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemContexto {
        private Integer rrtId;
        private Integer idTipoHabitacion;
        private String tipoHabitacion;
        private BigDecimal costoEstancia; // Total de la unidad
        private Integer capacidad;
        private List<BigDecimal> costoPorNoche;
    }
}