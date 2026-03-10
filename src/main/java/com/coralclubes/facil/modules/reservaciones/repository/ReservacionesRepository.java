package com.coralclubes.facil.modules.reservaciones.repository;

import com.coralclubes.facil.modules.clientes.dto.response.CuponDisponibleDto;
import com.coralclubes.facil.modules.reservaciones.dto.projection.DisponibilidadUnidadProjection;
import com.coralclubes.facil.modules.reservaciones.dto.request.AplicarPromocionRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.ConfirmarReservaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.AplicarPromocionResponse;
import com.coralclubes.facil.modules.reservaciones.dto.response.DisponibilidadUnidadDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.OpcionPagoPuntosDto;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReservacionesRepository {

    private final StoredProcedureExecutor spExecutor;

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

    public boolean eliminarReservaTemporal(UUID groupId) {
        Map<String, Object> params = new HashMap<>();
        params.put("GROUP_ID", groupId.toString());

        RowMapper<Integer> mapper = (rs, rowNum) -> rs.getInt("Exito");

        return spExecutor.querySingle("spResvEliminarReservaTemporal", params, mapper)
                .orElse(0) > 0;
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
}