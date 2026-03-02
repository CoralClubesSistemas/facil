package com.coralclubes.facil.modules.reservaciones.model.promociones.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReservacionContexto {
    private Integer idDesarrollo;
    private Integer idTipoHabitacion;
    private Integer idTemporada;
    private Integer idTipoTarifa;
    private BigDecimal montoActual;
    private LocalDateTime fechaEntrada;
    private Integer nochesTotales;
}