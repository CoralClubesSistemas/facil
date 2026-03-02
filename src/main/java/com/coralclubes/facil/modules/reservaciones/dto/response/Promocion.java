package com.coralclubes.facil.modules.reservaciones.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record Promocion(
        Integer idPromocion,
        String nombrePromocion,
        String descripcionPromocion,
        String codigoPromocion,
        Integer stockDisponible,
        Integer stockTotal,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        LocalDateTime fechaVisible,
        Boolean esGlobal,
        Boolean esPrivada,
        UUID uuidImagen,
        String urlImagen,
        List<Beneficio> beneficios,
        List<Regla> reglas
) {
    /**
     * Representa lo que se le otorgará al cliente (Ej. 10% de Descuento).
     */
    @Builder
    public record Beneficio(
            String tipoBeneficioClave, // Ej: "DESC_MONEY", "PCT_DESC"
            String tipoBeneficioDesc,  // Ej: "DESCUENTO EN DINERO"
            BigDecimal valor,          // Ej: 1500.00
            String objetivoClave,      // Ej: "TOTAL", "PRIMERA_NOCHE"
            String objetivoDesc        // Ej: "TOTAL"
    ) {}

    /**
     * Representa las condiciones que deben cumplirse (Ej. Solo Hotel Ixtapa).
     */
    @Builder
    public record Regla(
            String tipoReglaClave,     // Ej: "HOTEL", "TEMPORADA"
            String tipoReglaDesc,      // Ej: "HOTEL"
            String comparadorClave,    // Ej: "IN", ">="
            String comparadorDesc,     // Ej: "EN (LISTAS)"
            List<ReglaDetalle> detalles
    ) {}

    /**
     * Representa los valores específicos de la regla.
     */
    @Builder
    public record ReglaDetalle(
            Integer valorCatalogoId,   // Si la regla es por ID (Ej. ID del Hotel 1)
            String valorCatalogoDesc,  // (Ej. "Coral Ixtapa")
            BigDecimal valorNumerico,  // Si la regla es por monto (Ej. Compra mínima de $5,000)
            BigDecimal valorSecundario // Si la regla es un rango (Ej. BETWEEN $5,000 AND $10,000)
    ) {}
}