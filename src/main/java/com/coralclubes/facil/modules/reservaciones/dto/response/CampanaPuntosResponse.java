package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampanaPuntosResponse {
    private Integer idPromocion;
    private String nombre;
    private String descripcion;
    private String imagenUuid;
    private String imagenUrl;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaVisibilidad;
    private Integer clasificacionId;
    private String clasificacionDescripcion;
    private Integer temporadaId;
    private String temporadaDescripcion;
    private List<TabuladorPuntosResponse> tabuladorDetalleJson;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabuladorPuntosResponse {
        private Integer tabuladorId;
        private Integer desarrolloId;
        private String desarrolloNombre;
        private Integer rhdtId;
        private String habitacionNombre;
        private Integer puntosXNoche;
        private List<Integer> diasValidosJson;
    }
}