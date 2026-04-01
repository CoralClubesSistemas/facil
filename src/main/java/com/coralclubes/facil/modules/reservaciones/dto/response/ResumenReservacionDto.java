package com.coralclubes.facil.modules.reservaciones.dto.response;

public record ResumenReservacionDto(
    String membresia,
    Integer consecutivo,
    String nombreContacto,
    String emailContacto,
    String telefonoContacto,
    Integer desarrolloId,
    String nombreDesarrollo,
    Integer rhdtId,
    String tipoUnidad,
    String numeroUnidad,
    Integer idUnidadFisica,
    java.time.LocalDate fechaEntrada,
    java.time.LocalDate fechaSalida,
    Integer noches,
    String estatusClave,
    String estatusDescripcion,
    java.math.BigDecimal importeTotal,
    java.math.BigDecimal importePendiente,
    String ultimoReciboPagado
) {
}
