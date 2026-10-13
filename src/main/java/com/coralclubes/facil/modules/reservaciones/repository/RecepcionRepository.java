package com.coralclubes.facil.modules.reservaciones.repository;

import com.coralclubes.facil.modules.reservaciones.dto.request.*;
import com.coralclubes.facil.modules.reservaciones.dto.response.*;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class RecepcionRepository {

    private final StoredProcedureExecutor spExecutor;

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
            .folioTarjeta(rs.getObject("FolioTarjeta") != null ? rs.getInt("FolioTarjeta") : null)
            .build();

    private final RowMapper<EstadisticaDelDiaDto> estadisticasDiaMapper = (rs, rowNum) -> EstadisticaDelDiaDto.builder()
            .clave(rs.getString("Clave"))
            .nombre(rs.getString("Nombre"))
            .valor(rs.getInt("Valor"))
            .adicional(rs.getString("Adicional"))
            .build();

    private final RowMapper<UnidadDisponibleDto> unidadDisponibleMapper = (rs, rowNum) -> new UnidadDisponibleDto(
            rs.getInt("IdUnidad"),
            rs.getString("NumeroUnidad")
    );

    private final RowMapper<CatalogoCargoDto> catalogoCargoMapper = (rs, rowNum) -> new CatalogoCargoDto(
            rs.getInt("TipoMovimientoId"),
            rs.getString("Descripcion"),
            rs.getBigDecimal("Cuota")
    );

    private final RowMapper<MapaUnidadDto> mapaUnidadMapper = (rs, rowNum) -> new MapaUnidadDto(
            rs.getInt("UnidadId"),
            rs.getString("TipoUnidad"),
            rs.getString("NumeroUnidad"),
            rs.getInt("Piso"),
            rs.getInt("Capacidad"),
            rs.getString("EstatusClave"),
            rs.getString("EstatusDescripcion"),
            
            rs.getObject("FechaEntrada") != null ? rs.getTimestamp("FechaEntrada").toLocalDateTime() : null,
            rs.getObject("FechaSalida") != null ? rs.getTimestamp("FechaSalida").toLocalDateTime() : null,

            rs.getObject("FechaBloqueoInicial") != null ? rs.getTimestamp("FechaBloqueoInicial").toLocalDateTime() : null,
            rs.getObject("FechaBloqueoFinal") != null ? rs.getTimestamp("FechaBloqueoFinal").toLocalDateTime() : null,
            rs.getString("RazonBloqueo")
    );

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

    public void ejecutarCheckOut(CheckOutRequest request, String usuario) {
        Map<String, Object> params = Map.of(
                "Membresia", request.membresia(),
                "Consecutivo", request.consecutivo(),
                "Usuario", usuario
        );

        spExecutor.execute("spResvEjecutarCheckOut", params);
    }

    public List<CatalogoCargoDto> obtenerCatalogoCargosHabitacion(String membresia) {
        Map<String, Object> params = Map.of("Membresia", membresia);

        return spExecutor.queryList(
                "spResvCatalogoCargosHabitacion",
                params,
                catalogoCargoMapper
        );
    }

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

    public List<MapaUnidadDto> obtenerMapaUnidades(Integer desarrolloId) {
        return spExecutor.queryList(
                "spResvObtenerMapaUnidades",
                Map.of("Desarrollo", desarrolloId),
                mapaUnidadMapper
        );
    }

    public void transferirUnidad(TransferirUnidadRequest request, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", request.membresia());
        params.put("Consecutivo", request.consecutivo());
        params.put("NuevoRhdtId", request.nuevoRhdtId());
        params.put("NuevoRunId", request.nuevoRunId());
        params.put("ImporteDiferencia", request.importeDiferencia());
        params.put("Observaciones", request.observaciones());
        params.put("BloquearUnidadAnterior", request.bloquearUnidadAnterior());
        params.put("LimpiarUnidadAnterior", request.limpiarUnidadAnterior());
        params.put("Usuario", usuario);

        spExecutor.executeLog("spResvTransferirUnidad", params);
    }

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

    public void registrarMovimientoCheckInOutEspecial(CheckInOutEspecialRequest request, String usuario) {
        Map<String, Object> params = Map.of(
                "Membresia", request.membresia(),
                "Consecutivo", request.consecutivo(),
                "TipoOperacion", request.tipoOperacion().name(),
                "Usuario", usuario
        );

        spExecutor.executeLog("spResvRegistrarMovimientoCheckInOutEspecial", params);
    }
}
