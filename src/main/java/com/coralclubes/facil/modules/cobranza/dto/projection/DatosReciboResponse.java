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
    private String cadenaSeguridad;
    private String estatus; // Recibido desde el SP: 'ORIGINAL', 'REIMPRESION' o 'CANCELADO'
    private String reciboUuid;

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
