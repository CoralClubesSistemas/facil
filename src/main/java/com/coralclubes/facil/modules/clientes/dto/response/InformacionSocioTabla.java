package com.coralclubes.facil.modules.clientes.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record InformacionSocioTabla(
        // Identidad
        String membresia,
        String nombreCompleto,
        String nombre,
        String segundoNombre,
        String apellidoPaterno,
        String apellidoMaterno,
        LocalDate fechaNacimiento,

        // Comunicación
        String correo,
        String correoAlternativo,
        String telefono,
        String telefonoAlternativo,

        // Indicadores Financieros y Cobranza
        BigDecimal saldoFinMes,
        String tipoTarjetaAfiliada,
        String ejecutivoAsignado,
        String ultimoPQAPagado,

        // Puntos
        BigDecimal puntosDisponibles,
        BigDecimal puntosConsumidos,

        // Beneficiarios
        Integer totalBenefActivos,
        String nombresBeneficiarios,

        // Configuración de Membresía
        Integer tipoMembresiaId,
        String tipoMembresia,
        Integer clasificacionMembresiaId,
        String clasificacionMembresia,

        // Desarrollo y Estatus
        Integer desarrolloId,
        String desarrollo,
        Integer estatusMembresiaId,
        String estatusMembresia,

        // Cartera y Vigencia
        Integer carteraCobranzaId,
        String carteraCobranza,
        String vigenciaOriginal,
        String tiempoRestante,

        // Metadatos de Paginación (Angular)
        Integer totalRegistros
) {}