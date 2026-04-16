package com.coralclubes.facil.modules.cobranza.dto.projection;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatosReciboResponse {
    private String empresa;
    private String rfcEmpresa;
    private String direccionEmpresa;
    private String telefonoEmpresa;
    private String webEmpresa;
    private String folio;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime fecha;

    private String moneda;
    private String membresia;
    private String clienteNombre;
    private String direccionCliente;
    private BigDecimal subtotal;
    private BigDecimal descuentoTotal;
    private BigDecimal total;
    private String cadenaSeguridad;
    private List<MovimientosHijos> movimientos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovimientosHijos {
        private String descripcion;
        private String referencia;
        private BigDecimal importe;
        private BigDecimal interes;
        private BigDecimal descuento;
        private BigDecimal totalNeto;
    }
}
