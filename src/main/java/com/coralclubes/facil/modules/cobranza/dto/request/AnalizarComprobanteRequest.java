package com.coralclubes.facil.modules.cobranza.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Solicitud del frontend para analizar un comprobante bancario.
 *
 * @param fileId Identificador UUID del archivo en Coral Almacenamiento.
 * @param numeroPagina Número de página a analizar (1-indexed). Opcional, por defecto página 1.
 */
public record AnalizarComprobanteRequest(
        @NotNull(message = "El fileId del archivo es obligatorio.")
        UUID fileId,

        @Min(value = 1, message = "El número de página debe ser igual o mayor a 1.")
        @DefaultValue("1")
        Integer numeroPagina,

        @NotNull(message = "El id de banco es obligatorio")
        Integer idBanco,

        @NotNull(message = "La fecha de busqueda es obligatoria")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fechaDeposito,

        @NotNull(message = "El monto es obligatorio")
        BigDecimal monto
) {}
