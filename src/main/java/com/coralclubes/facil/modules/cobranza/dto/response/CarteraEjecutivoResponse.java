package com.coralclubes.facil.modules.cobranza.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CarteraEjecutivoResponse(
        Integer totalRegistros,
        String membresia,
        String nombreCompleto,
        String nombre,
        String segundoNombre,
        String apellidoPaterno,
        String apellidoMaterno,
        LocalDate fechaNacimiento,
        String correo,
        String correoAlternativo,
        String telefono,
        String telefonoAlternativo,
        BigDecimal saldoFinMes,
        String tipoTarjetaAfiliada,
        String ejecutivoAsignado,
        String ultimoPQAPagado,
        BigDecimal puntosDisponibles,
        BigDecimal puntosConsumidos,
        Integer totalBenefActivos,
        String nombresBeneficiarios,
        Integer tipoMembresiaId,
        String tipoMembresia,
        Integer clasificacionMembresiaId,
        String clasificacionMembresia,
        Integer desarrolloId,
        String desarrollo,
        Integer estatusMembresiaId,
        String estatusMembresia,
        Integer carteraCobranzaId,
        String carteraCobranza,
        String vigenciaOriginal,
        String tiempoRestante
) {
}

