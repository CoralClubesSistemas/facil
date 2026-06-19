package com.coralclubes.facil.modules.cobranza.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Dto usado para obtener toda la informacion relacionada con el recibo digital y generar los pdfs correspondientes
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatosReciboResponse {
    private Integer numeroRecibo;
    private Integer idSerieRecibo;

    private String empresa;
    private String rfcEmpresa;
    private String direccionEmpresa;
    private String telefonoEmpresa;
    private String webEmpresa;
    private String correoEmpresa;

    private String folio;
    private String fecha;

    private String membresia;
    private String clienteNombre;
    private String direccionSocio;
    private BigDecimal subtotal;
    private BigDecimal totalIva;
    private BigDecimal descuentoTotal;
    private BigDecimal total;
    private List<MovimientosHijos> movimientos;

    private String desarrollo;
    private String producto;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovimientosHijos {
        private String descripcion;
        private String referencia;
        private BigDecimal importe;
        private BigDecimal interes;
        private BigDecimal iva;
        private BigDecimal descuento;
        private BigDecimal totalNeto;
    }
}
