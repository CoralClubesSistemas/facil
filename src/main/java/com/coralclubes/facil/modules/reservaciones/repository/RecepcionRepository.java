package com.coralclubes.facil.modules.reservaciones.repository;

import com.coralclubes.facil.modules.reservaciones.dto.request.*;
import com.coralclubes.facil.modules.reservaciones.dto.response.*;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class RecepcionRepository {
    private final StoredProcedureExecutor spExecutor;

    // =========================================================================
    // MÓDULO RECEPCIÓN (FRONT DESK)
    // =========================================================================

    private final RowMapper<OperacionDiaDto> operacionesDiaMapper = (rs, rowNum) -> OperacionDiaDto.builder()
            .membresia(rs.getString("Membresia"))
            .consecutivo(rs.getInt("Consecutivo"))
            .nombreHuesped(rs.getString("NombreHuesped"))
            .esSocio(rs.getBoolean("EsSocio"))
            .rhdtId(rs.getInt("RhdtId"))
            .tipoUnidad(rs.getString("TipoUnidad"))
            .numeroHabitacion(rs.getString("NumeroHabitacion"))
            .fechaEntrada(rs.getDate("FechaEntrada").toLocalDate())
            .fechaSalida(rs.getDate("FechaSalida").toLocalDate())
            .estatusReservacion(rs.getString("EstatusReservacion"))
            .descripcionEstatus(rs.getString("DescripcionEstatus"))
            .importePendiente(rs.getBigDecimal("ImportePendiente"))
            .ultimoReciboPagado(rs.getString("UltimoReciboPagado"))
            .build();

    private final RowMapper<EstadisticaDelDiaDto> estadisticasDiaMapper = (rs, rowNum) -> EstadisticaDelDiaDto.builder()
            .clave(rs.getString("Clave"))
            .nombre(rs.getString("Nombre"))
            .valor(rs.getInt("Valor"))
            .adicional(rs.getString("Adicional"))
            .border(rs.getString("Border"))
            .icono(rs.getString("Icono"))
            .text(rs.getString("Text"))
            .bg(rs.getString("Bg"))
            .build();
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    RowMapper<UnidadDisponibleDto> unidadDisponibleMapper = (rs, rowNum) -> new UnidadDisponibleDto(
            rs.getInt("IdUnidad"),
            rs.getString("NumeroUnidad")
    );

    RowMapper<CatalogoCargoDto> catalogoCargoMapper = (rs, rowNum) -> new CatalogoCargoDto(
            rs.getInt("TipoMovimientoId"),
            rs.getString("Descripcion"),
            rs.getBigDecimal("Cuota")
    );

    RowMapper<MapaUnidadDto> mapaUnidadMapper = (rs, rowNum) -> new MapaUnidadDto(
            rs.getInt("UnidadId"),
            rs.getString("TipoUnidad"),
            rs.getString("NumeroUnidad"),
            rs.getInt("Piso"),
            rs.getInt("Capacidad"),
            rs.getString("EstatusClave"),
            rs.getString("EstatusDescripcion")
    );

    private final RowMapper<String> stringMapper = (rs, rowNum) -> rs.getString("Descripcion");

    public List<OperacionDiaDto> obtenerOperacionesDelDia(Integer desarrolloId) {
        return spExecutor.queryList(
                "spResvObtenerOperacionesDelDia",
                Map.of("Desarrollo", desarrolloId),
                operacionesDiaMapper
        );
    }

    public List<EstadisticaDelDiaDto> obtenerEstadisticasDelDia(Integer desarrolloId) {
        return spExecutor.queryList(
                "spResvObtenerEstadisticasDelDia",
                Map.of("Desarrollo", desarrolloId),
                estadisticasDiaMapper
        );
    }

    public void ejecutarCheckIn(CheckInRequest request, String usuario) {
        Map<String, Object> params = Map.of(
                "Membresia", request.membresia(),
                "Consecutivo", request.consecutivo(),
                "IdUnidad", request.idUnidad(),
                "Usuario", usuario
        );
        spExecutor.execute("spResvEjecutarCheckIn", params);
    }

    /**
     * Obtiene las habitaciones físicas que están limpias y disponibles
     * para un tipo de unidad específico, perdonando la auto-colisión de la reserva actual.
     */
    public List<UnidadDisponibleDto> obtenerUnidadesDisponiblesParaCheckIn(String membresia, Integer consecutivo, Integer tipoUnidadId) {
        Map<String, Object> params = Map.of(
                "Membresia", membresia,
                "Consecutivo", consecutivo,
                "TipoUnidad", tipoUnidadId
        );

        return spExecutor.queryList(
                "spResvObtenerUnidadesDisponiblesParaCheckIn",
                params,
                unidadDisponibleMapper
        );
    }

    /**
     * Ejecuta el SP de Check-Out. Validará en BD que no haya saldos pendientes.
     */
    public void ejecutarCheckOut(CheckOutRequest request, String usuario) {
        Map<String, Object> params = Map.of(
                "Membresia", request.membresia(),
                "Consecutivo", request.consecutivo(),
                "Usuario", usuario
        );

        spExecutor.execute("spResvEjecutarCheckOut", params);
    }


    /**
     * Obtiene el listado de cargos posibles (con sus cuotas vigentes) para una membresía.
     */
    public List<CatalogoCargoDto> obtenerCatalogoCargosHabitacion(String membresia) {
        Map<String, Object> params = Map.of("Membresia", membresia);

        return spExecutor.queryList(
                "spResvCatalogoCargosHabitacion",
                params,
                catalogoCargoMapper
        );
    }

    /**
     * Ejecuta el SP que inserta un nuevo cargo financiero a la cuenta de la habitación.
     */
    public void generarCargoHabitacion(GenerarCargoRequest request, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", request.membresia());
        params.put("Consecutivo", request.consecutivo());
        params.put("TipoMovimiento", request.tipoMovimiento());
        params.put("Usuario", usuario);
        params.put("Importe", request.importe());
        params.put("Referencia", request.referencia());
        params.put("Observaciones", request.observaciones());

        spExecutor.execute("spResvGenerarCargoHabitacion", params);
    }

    /**
     * Obtiene el listado completo de las habitaciones físicas de un desarrollo
     * junto con su estado actual (Ocupada, Libre, Mantenimiento, etc.).
     */
    public List<MapaUnidadDto> obtenerMapaUnidades(Integer desarrolloId) {
        return spExecutor.queryList(
                "spResvObtenerMapaUnidades",
                Map.of("Desarrollo", desarrolloId),
                mapaUnidadMapper
        );
    }

    /**
     * Obtiene el listado de las acciones más recientes del día para el Timeline.
     */
    public List<String> obtenerActividadDiaria(Integer desarrolloId) {
        return spExecutor.queryList(
                "spResvObtenerActividadDiaria",
                Map.of("DesarrolloId", desarrolloId),
                stringMapper
        );
    }

    /**
     * Ejecuta el SP para transferir a un huésped de habitación.
     * Libera (ensucia) la actual, ocupa la nueva, gestiona padres/hijos
     * y genera un cargo de upgrade si aplica.
     */
    public void transferirUnidad(TransferirUnidadRequest request, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", request.membresia());
        params.put("Consecutivo", request.consecutivo());
        params.put("NuevoRhdtId", request.nuevoRhdtId());
        params.put("NuevoRunId", request.nuevoRunId());
        params.put("ImporteDiferencia", request.importeDiferencia());
        params.put("Observaciones", request.observaciones());
        params.put("BloquearUnidadAnterior", request.bloquearUnidadAnterior());
        params.put("Usuario", usuario);

        spExecutor.executeLog("spResvTransferirUnidad", params);
    }

    /**
     * Consulta la función escalar para saber de cuánto sería la penalización
     * si se cancela la reserva en este momento.
     */
    public BigDecimal calcularPenalizacionCancelacion(String membresia, Integer consecutivo) {
        String sql = "SELECT dbo.fnResvCalcularPenalizacionCancelacion(:Membresia, :Consecutivo)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("Membresia", membresia)
                .addValue("Consecutivo", consecutivo);

        // Usamos jdbcTemplate para consultar funciones escalares directamente
        BigDecimal penalizacion = namedParameterJdbcTemplate.queryForObject(sql, params, BigDecimal.class);
        return penalizacion != null ? penalizacion : BigDecimal.ZERO;
    }

    private final RowMapper<CheckInOutEspecialCotizacionDto> cotizacionCheckInOutMapper = (rs, rowNum) -> CheckInOutEspecialCotizacionDto.builder()
            .aplicaCheckinAnticipado(rs.getBoolean("AplicaCheckinAnticipado"))
            .minutosAntesCheckin(rs.getInt("MinutosAntesCheckin"))
            .cargoCheckin(rs.getBigDecimal("CargoCheckin"))
            .yaTieneCargoCheckin(rs.getBoolean("YaTieneCargoCheckin"))
            .aplicaCheckoutPosterior(rs.getBoolean("AplicaCheckoutPosterior"))
            .minutosDespuesCheckout(rs.getInt("MinutosDespuesCheckout"))
            .cargoCheckout(rs.getBigDecimal("CargoCheckout"))
            .yaTieneCargoCheckout(rs.getBoolean("YaTieneCargoCheckout"))
            .fechaEntrada(rs.getTimestamp("FechaEntrada") != null ? rs.getTimestamp("FechaEntrada").toLocalDateTime() : null)
            .fechaSalida(rs.getTimestamp("FechaSalida") != null ? rs.getTimestamp("FechaSalida").toLocalDateTime() : null)
            .minutosMaxCheckin(rs.getInt("MinutosMaxCheckin"))
            .minutosMaxCheckout(rs.getInt("MinutosMaxCheckout"))
            .build();

    /**
     * Ejecuta el SP que evalúa si aplica cargo por Check-in anticipado o Check-out posterior,
     * y devuelve la cotización sin generar ningún movimiento.
     */
    public CheckInOutEspecialCotizacionDto cotizarCheckInOutEspecial(String membresia, Integer consecutivo) {
        Map<String, Object> params = Map.of(
                "Membresia", membresia,
                "Consecutivo", consecutivo
        );

        return spExecutor.querySingle(
                "spResvCotizarCheckInOutEspecial",
                params,
                cotizacionCheckInOutMapper
        ).orElse(null);
    }

    /**
     * Ejecuta el SP que registra un cargo por Check-in anticipado o Check-out posterior.
     * El SP valida umbrales de horas y disponibilidad de la unidad antes de generar el cargo.
     */
    public void registrarMovimientoCheckInOutEspecial(CheckInOutEspecialRequest request, String usuario) {
        Map<String, Object> params = Map.of(
                "Membresia", request.membresia(),
                "Consecutivo", request.consecutivo(),
                "TipoOperacion", request.tipoOperacion().name(),
                "Usuario", usuario
        );

        spExecutor.executeLog("spResvRegistrarMovimientoCheckInOutEspecial", params);
    }

    /**
     * Ejecuta el SP que cancela la reservación, libera los cuartos,
     * abona el saldo pendiente y carga la penalización si aplica.
     */
    public void cancelarReservacion(CancelarReservacionRequest request, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", request.membresia());
        params.put("Consecutivo", request.consecutivo());
        params.put("MotivoCancelacion", request.motivoCancelacion());
        params.put("CobrarCuotaCancelacion", request.cobrarCuotaCancelacion() ? 1 : 0);
        params.put("Usuario", usuario);

        spExecutor.executeLog("spResvCancelarReservacion", params);
    }
}
