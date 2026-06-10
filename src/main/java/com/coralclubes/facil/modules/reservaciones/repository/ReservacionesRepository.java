package com.coralclubes.facil.modules.reservaciones.repository;

import com.coralclubes.facil.modules.clientes.dto.response.CuponDisponibleDto;
import com.coralclubes.facil.modules.reservaciones.dto.projection.DisponibilidadUnidadProjection;
import com.coralclubes.facil.modules.reservaciones.dto.request.*;
import com.coralclubes.facil.modules.reservaciones.dto.response.*;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.utils.json.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class ReservacionesRepository {

    private final StoredProcedureExecutor spExecutor;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final RowMapper<DisponibilidadUnidadProjection> disponibilidadMapper = (rs, rowNum) -> {
        String uuidStr = rs.getString("uuidImagen");

        return new DisponibilidadUnidadProjection(
                rs.getInt("idTipoUnidad"),
                rs.getString("nombreUnidad"),
                rs.getString("descripcionCorta"),
                rs.getInt("capacidad"),
                rs.getInt("stockDisponible"),
                rs.getBigDecimal("costoEstancia"),
                uuidStr != null ? UUID.fromString(uuidStr) : null
        );
    };

    RowMapper<UUID> uuidMapper = (rs, rowNum) -> {
        String uuidStr = rs.getString("GROUP_ID_OUT");
        return uuidStr != null ? UUID.fromString(uuidStr) : null;
    };

    RowMapper<CuponDisponibleDto> cuponMapper = (rs, rowNum) -> new CuponDisponibleDto(
            rs.getString("tipoDescuento"),
            rs.getInt("paqueteId"),
            rs.getInt("consecutivo"),
            rs.getBigDecimal("porcentajeDescuento")
    );

    RowMapper<DetalleReservacionDto> detalleReservacionMapper = (rs, rowNum) -> {

        // Extraemos y parseamos el JSON de Cargos (puede venir NULL si la tabla de cargos estuviera vacía)
        String cargosJsonStr = rs.getString("CargosJson");
        List<CargoHabitacionDto> listadoCargos = (cargosJsonStr != null && !cargosJsonStr.isBlank())
                ? JsonUtils.fromJson(cargosJsonStr, new TypeReference<>() {
        }) : List.of();

        // Extraemos y parseamos el JSON de transferencias (puede venir NULL si no hay transferencias)
        String transferenciasJsonStr = rs.getString("TransferenciasJson");
        List<TransferenciaHabitacionDto> listadoTransferencias = (transferenciasJsonStr != null && !transferenciasJsonStr.isBlank())
                ? JsonUtils.fromJson(transferenciasJsonStr, new TypeReference<>() {
        }) : List.of();

        return DetalleReservacionDto.builder()
                .membresia(rs.getString("Membresia"))
                .consecutivo(rs.getInt("Consecutivo"))
                .desarrolloId(rs.getInt("DesarrolloId"))
                .nombreDesarrollo(rs.getString("NombreDesarrollo"))
                .nombreHuesped(rs.getString("NombreHuesped"))
                .esSocio(rs.getBoolean("EsSocio"))
                .rhdtId(rs.getInt("RhdtId"))
                .tipoUnidad(rs.getString("TipoUnidad"))
                .idUnidad(rs.getInt("IdUnidad"))
                .numeroHabitacion(rs.getString("NumeroHabitacion"))
                .fechaEntrada(rs.getDate("FechaEntrada").toLocalDate())
                .fechaSalida(rs.getDate("FechaSalida").toLocalDate())

                // Extraemos las nuevas fechas (Pueden ser null si aún no hace checkin/out)
                .fechaHoraCheckIn(rs.getTimestamp("FechaHoraCheckIn") != null ? rs.getTimestamp("FechaHoraCheckIn").toLocalDateTime() : null)
                .fechaHoraCheckOut(rs.getTimestamp("FechaHoraCheckOut") != null ? rs.getTimestamp("FechaHoraCheckOut").toLocalDateTime() : null)

                .estatusClave(rs.getString("EstatusClave"))
                .estatusDescripcion(rs.getString("EstatusDescripcion"))
                .importeTotal(rs.getBigDecimal("ImporteTotal"))
                .importePendiente(rs.getBigDecimal("ImportePendiente"))
                .ultimoReciboPagado(rs.getString("UltimoReciboPagado"))
                .promocionAplicada(rs.getString("PromocionAplicada"))
                .cuponPaqueteId(rs.getInt("CuponPaqueteId"))
                .puntosConsumidos(rs.getInt("PuntosConsumidos"))
                .peticionesEspeciales(rs.getString("PeticionesEspeciales"))
                .numeroSocios(rs.getInt("NumeroSocios"))

                // Inyectamos la lista de cargos ya parseada
                .cargos(listadoCargos)

                // Historial de transferencias de unidades
                .cantidadTransferencias(rs.getInt("CantidadTransferencias"))
                .haSidoTransferida(rs.getBoolean("HaSidoTransferida"))
                .transferenciasHistorial(listadoTransferencias)

                .build();
    };

    private final RowMapper<ResumenReservacionDto> resumenReservacionMapper = (rs, rowNum) -> new ResumenReservacionDto(
            rs.getString("Membresia"),
            rs.getInt("Consecutivo"),
            rs.getString("NombreContacto"),
            rs.getString("EmailContacto"),
            rs.getString("TelefonoContacto"),
            rs.getInt("DesarrolloId"),
            rs.getString("NombreDesarrollo"),
            rs.getInt("RhdtId"),
            rs.getString("TipoUnidad"),
            rs.getString("NumeroUnidad"),
            rs.getInt("IdUnidadFisica"),
            rs.getDate("FechaEntrada") != null ? rs.getDate("FechaEntrada").toLocalDate() : null,
            rs.getDate("FechaSalida") != null ? rs.getDate("FechaSalida").toLocalDate() : null,
            rs.getInt("Noches"),
            rs.getString("EstatusClave"),
            rs.getString("EstatusDescripcion"),
            rs.getBigDecimal("ImporteTotal"),
            rs.getBigDecimal("ImportePendiente"),
            rs.getString("UltimoReciboPagado")
    );

    private final RowMapper<CargoHabitacionDto> cargoHabitacionMapper = (rs, rowNum) -> new CargoHabitacionDto(
            rs.getInt("IdMovimiento"),
            rs.getString("Descripcion"),
            rs.getBigDecimal("ImporteCargo"),
            rs.getBigDecimal("ImportePendiente"),
            rs.getTimestamp("FechaRegistro") != null ? rs.getTimestamp("FechaRegistro").toLocalDateTime() : null,
            rs.getTimestamp("fechaPagoRecibo") != null ? rs.getTimestamp("fechaPagoRecibo").toLocalDateTime() : null,
            rs.getString("recibo")
    );

    // =========================================================================
    // MAPPERS PARA CONSULTA GENERAL Y RECEPCION REFACTOR
    // =========================================================================

    private final RowMapper<ReservacionHistoricaDto> reservacionHistoricaMapper = (rs, rowNum) -> ReservacionHistoricaDto.builder()
            .totalRegistros(rs.getInt("TotalRegistros"))
            .membresia(rs.getString("Membresia"))
            .consecutivo(rs.getInt("Consecutivo"))
            .nombreDesarrollo(rs.getString("NombreDesarrollo"))
            .nombreHuesped(rs.getString("NombreHuesped"))
            .tipoUnidad(rs.getString("TipoUnidad"))
            .numeroHabitacion(rs.getString("NumeroHabitacion"))
            .fechaEntrada(rs.getDate("FechaEntrada").toLocalDate())
            .fechaSalida(rs.getDate("FechaSalida").toLocalDate())
            .fechaRegistro(rs.getTimestamp("FechaRegistro").toLocalDateTime())
            .estatusClave(rs.getString("EstatusClave"))
            .estatusDescripcion(rs.getString("EstatusDescripcion"))
            .importeTotal(rs.getBigDecimal("ImporteTotal"))
            .importePendiente(rs.getBigDecimal("ImportePendiente"))
            .build();

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


    public List<DisponibilidadUnidadProjection> buscarDisponibilidad(
            Integer destinoId,
            LocalDate fechaEntrada,
            LocalDate fechaSalida,
            Integer personas,
            String membresia
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("DestinoId", destinoId);
        params.put("FechaEntrada", fechaEntrada);
        params.put("FechaSalida", fechaSalida);
        params.put("Personas", personas);
        params.put("Membresia", membresia);

        return spExecutor.queryList("spResvBuscarDisponibilidadTiposUnidades", params, disponibilidadMapper);
    }

    public UUID spResvCrearReservaTemporal(
            String jsonCarrito,
            LocalDate fechaEntrada,
            LocalDate fechaSalida,
            String cliente,
            String ipAddress
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("JSON_CARRITO", jsonCarrito);
        params.put("FECHA_ENTRADA", fechaEntrada);
        params.put("FECHA_SALIDA", fechaSalida);
        params.put("CLIENTE", cliente);
        params.put("IP_ADDRESS", ipAddress);

        return spExecutor.querySingle("spResvCrearReservaTemporal", params, uuidMapper)
                .orElseThrow(() -> new RuntimeException("Error en base de datos: No se generó el UUID del carrito."));
    }

    public void eliminarReservaTemporal(UUID groupId) {
        Map<String, Object> params = new HashMap<>();
        params.put("GROUP_ID", groupId.toString());

        RowMapper<Integer> mapper = (rs, rowNum) -> rs.getInt("Exito");

        spExecutor.querySingle("spResvEliminarReservaTemporal", params, mapper);
    }

    public String obtenerContextoReservaTemporalJson(UUID groupId) {
        Map<String, Object> params = new HashMap<>();
        params.put("GROUP_ID", groupId.toString());

        // Mapeamos la única columna que devuelve el SP (ContextoJson)
        RowMapper<String> jsonMapper = (rs, rowNum) -> rs.getString("ContextoJson");

        return spExecutor.querySingle("spResvObtenerContextoReservaTemporal", params, jsonMapper)
                .orElse(null);
    }

    public String obtenerDesgloseFinancieroJson(UUID groupId) {
        Map<String, Object> params = new HashMap<>();
        params.put("GROUP_ID", groupId.toString());

        RowMapper<String> jsonMapper = (rs, rowNum) -> rs.getString("DesgloseJson");

        return spExecutor.querySingle("spResvObtenerDesgloseFinanciero", params, jsonMapper).orElse(null);
    }

    public List<CuponDisponibleDto> obtenerCuponesCarrito(UUID groupId) {
        Map<String, Object> params = new HashMap<>();
        params.put("GROUP_ID", groupId.toString());

        return spExecutor.queryList("spResvObtenerCuponesCarrito", params, cuponMapper);
    }

    // 1. Guardar la reserva principal
    public List<Integer> guardarReservacionFisica(ConfirmarReservaRequest request, String usuario, String detalleJson) {
        Map<String, Object> params = new HashMap<>();
        params.put("GroupId", request.groupId().toString());
        params.put("Email", request.email());
        params.put("Email2", request.email2());
        params.put("Telefono1", request.telefono1());
        params.put("Telefono2", request.telefono2());
        params.put("NombreReserva", request.nombreReserva());
        params.put("PeticionEspecial", request.peticionEspecial());
        params.put("Usuario", usuario);
        params.put("ReservacionPortal", 0); // 0 = Panel Interno, 1 = Web
        params.put("DetalleJson", detalleJson);

        // El SP devuelve una tabla con MovimientoId y ReservacionConsecutivo
        RowMapper<Integer> mapper = (rs, rowNum) -> rs.getInt("ReservacionConsecutivo");

        return spExecutor.queryListLog("spResvGuardarReservacion", params, mapper, usuario, true, false);
    }

    // 2. Quemar Promoción
    public void registrarConsumoPromocion(String membresia, Integer consecutivo, String codigoPromocion, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("ConsecutivoReservacion", consecutivo);
        params.put("CodigoPromocion", codigoPromocion);
        params.put("Usuario", usuario);

        spExecutor.execute("spResvDetallarConsumoOferta", params);
    }

    // 3. Quemar Cupón
    public void consumirCuponReservacion(String membresia, Integer paqueteId, Integer consecutivoCupon, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("PaqueteId", paqueteId);
        params.put("Consecutivo", consecutivoCupon);
        params.put("Usuario", usuario);

        spExecutor.execute("spResvConsumirCuponReservacion", params);
    }

    public DetalleReservacionDto obtenerDetalleReservacion(String membresia, Integer consecutivo) {
        Map<String, Object> params = Map.of(
                "Membresia", membresia,
                "Consecutivo", consecutivo
        );
        return spExecutor.querySingle("spResvObtenerDetalleReservacion", params, detalleReservacionMapper)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la reservación solicitada."));
    }

    /**
     * Obtiene el listado desglosado de todos los cargos financieros de una reservación.
     * Ideal para el módulo de Caja / Cobranza.
     */
    public List<CargoHabitacionDto> obtenerCargosReservacion(String membresia, Integer consecutivo) {
        Map<String, Object> params = Map.of(
                "Membresia", membresia,
                "Consecutivo", consecutivo
        );

        return spExecutor.queryList(
                "spResvObtenerCargosReservacion",
                params,
                cargoHabitacionMapper
        );
    }

    public DisponibilidadUnidadProjection obtenerDisponibilidadUnidadEspecifica(
            Integer tipoUnidadId,
            LocalDate fechaEntrada,
            LocalDate fechaSalida,
            String membresia) {
        Map<String, Object> params = new HashMap<>();
        params.put("RhdtId", tipoUnidadId);
        params.put("FechaEntrada", fechaEntrada);
        params.put("FechaSalida", fechaSalida);
        params.put("Membresia", membresia);

        return spExecutor.querySingleLog("spResvCotizarTipoUnidadEspecifica", params, disponibilidadMapper)
                .orElse(null);
    }

    public ResumenReservacionDto obtenerResumenReservacion(String membresia, Integer consecutivo) {
        Map<String, Object> params = Map.of(
                "Membresia", membresia,
                "Consecutivo", consecutivo
        );

        return spExecutor.querySingle("spResvObtenerResumenReservacion", params, resumenReservacionMapper)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el resumen de la reservación solicitada."));
    }

    public Optional<ResumenReservacionDto> spResvObtenerReservacionXMovimiento(String membresia, Integer movimiento) {
        Map<String, Object> params = Map.of(
                "Membresia", membresia,
                "MvtId", movimiento
        );

        return spExecutor.querySingle("spResvObtenerReservacionXMovimiento", params, resumenReservacionMapper);
    }

    public void spResvActualizarReservacionPagada(
            String membresia,
            Integer numeroReservacion
    ) {
        Map<String, Object> params = Map.of(
                "Membresia", membresia,
                "NumeroReservacion", numeroReservacion
        );

        spExecutor.execute("spResvActualizarReservacionPagada", params);
    }

    // =========================================================================
    // MAPPERS ADICIONALES REFACTOR
    // =========================================================================

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

    // =========================================================================
    // METODOS DE CONSULTA GENERAL Y RECEPCION REFACTOR
    // =========================================================================

    public List<ReservacionHistoricaDto> consultarHistoricoReservaciones(FiltroConsultaGeneral filtro) {
        Map<String, Object> params = new HashMap<>();
        params.put("DesarrolloId", filtro.desarrolloId());
        params.put("FechaInicio", filtro.fechaInicio());
        params.put("FechaFin", filtro.fechaFin());
        params.put("TipoFecha", filtro.tipoFecha());
        params.put("EstatusClave", filtro.estatusClave());
        params.put("Busqueda", filtro.busqueda());
        params.put("PageNumber", filtro.pageNumber());
        params.put("PageSize", filtro.pageSize());

        return spExecutor.queryList("spResvConsultaGeneralReservaciones", params, reservacionHistoricaMapper);
    }

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

    public List<String> obtenerActividadDiaria(Integer desarrolloId) {
        return spExecutor.queryList(
                "spResvObtenerActividadDiaria",
                Map.of("DesarrolloId", desarrolloId),
                stringMapper
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

    public BigDecimal calcularPenalizacionCancelacion(String membresia, Integer consecutivo) {
        String sql = "SELECT dbo.fnResvCalcularPenalizacionCancelacion(:Membresia, :Consecutivo)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("Membresia", membresia)
                .addValue("Consecutivo", consecutivo);

        BigDecimal penalizacion = namedParameterJdbcTemplate.queryForObject(sql, params, BigDecimal.class);
        return penalizacion != null ? penalizacion : BigDecimal.ZERO;
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

    public void cancelarReservacion(CancelarReservacionRequest request, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", request.membresia());
        params.put("Consecutivo", request.consecutivo());
        params.put("MotivoCancelacion", request.motivoCancelacion());
        params.put("CobrarCuotaCancelacion", request.cobrarCuotaCancelacion() ? 1 : 0);
        params.put("Usuario", usuario);

        spExecutor.executeLog("spResvCancelarReservacion", params);
    }

    public String spResvCrearMembresiaExterno(CrearMembresiaExternoRequest request, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("desarrollo", request.desarrollo());
        params.put("nombre", request.nombre());
        params.put("segundo_nombre", request.segundoNombre());
        params.put("apellido_paterno", request.apellidoPaterno());
        params.put("apellido_materno", request.apellidoMaterno());
        params.put("email_principal", request.emailPrincipal());
        params.put("telefono_principal", request.telefonoPrincipal());
        params.put("email_secundario", request.emailSecundario());
        params.put("telefono_secundario", request.telefonoSecundario());
        params.put("usuario", usuario);

        RowMapper<String> mapper = (rs, rowNum) -> rs.getString("MembresiaReserva");

        return spExecutor.querySingle("spResvCrearMembresiaExterno", params, mapper)
                .orElseThrow(() -> new RuntimeException("Error en base de datos: No se generó la membresía."));
    }
}