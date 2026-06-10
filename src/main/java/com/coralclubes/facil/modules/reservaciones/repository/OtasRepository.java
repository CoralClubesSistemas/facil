package com.coralclubes.facil.modules.reservaciones.repository;

import com.coralclubes.facil.modules.reservaciones.dto.response.ConfiguracionOtaResponse;
import com.coralclubes.facil.modules.reservaciones.dto.response.GenerarReservacionOtaResponse;
import com.coralclubes.facil.modules.reservaciones.dto.response.UnidadOtaResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OtasRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<Integer> scalarIntMapper = (rs, rowNum) -> rs.getInt(1);

    private final RowMapper<ConfiguracionOtaResponse> configuracionOtaMapper = (rs, rowNum) -> ConfiguracionOtaResponse.builder()
            .idConfiguracionOta(rs.getInt("id_configuracion_ota"))
            .lsvOta(rs.getInt("lsv_ota"))
            .nombreOta(rs.getString("nombre_ota"))
            .idDesarrollo(rs.getInt("id_desarrollo"))
            .nombreDesarrollo(rs.getString("nombre_desarrollo"))
            .porcentajeComision(rs.getBigDecimal("porcentaje_comision"))
            .fechaInicio(rs.getDate("fecha_inicio") != null ? rs.getDate("fecha_inicio").toLocalDate() : null)
            .fechaFin(rs.getDate("fecha_fin") != null ? rs.getDate("fecha_fin").toLocalDate() : null)
            .build();

    private final RowMapper<UnidadOtaResponse> unidadOtaMapper = (rs, rowNum) -> UnidadOtaResponse.builder()
            .idUnidad(rs.getInt("id_unidad"))
            .nombreUnidad(rs.getString("nombre_unidad"))
            .tipoUnidad(rs.getString("tipo_unidad"))
            .capacidadUnidad(rs.getInt("capacidad_unidad"))
            .build();

    private final RowMapper<GenerarReservacionOtaResponse> generarReservacionMapper = (rs, rowNum) -> GenerarReservacionOtaResponse.builder()
            .membresia(rs.getString("membresia"))
            .consecutivo(rs.getInt("consecutivo"))
            .idMovimiento(rs.getInt("id_movimiento"))
            .build();

    public void spResvAgregarNuevaOta(String nombreOta) {
        Map<String, Object> params = Map.of(
                "nombre_ota", nombreOta
        );
        spExecutor.execute("spResvAgregarNuevaOta", params);
    }

    public Optional<Integer> spResvCrearConfiguracionOta(
            Integer idOta,
            Integer idDesarrollo,
            java.time.LocalDate fechaInicio,
            java.time.LocalDate fechaFin,
            BigDecimal porcentajeComision,
            String usuario
    ) {
        Map<String, Object> params = Map.of(
                "id_ota", idOta,
                "id_desarrollo", idDesarrollo,
                "fecha_inicio", fechaInicio,
                "fecha_fin", fechaFin,
                "porcentaje_comision", porcentajeComision,
                "usuario", usuario
        );
        // Crear configuración de OTA utiliza logs (con log y éxito)
        return spExecutor.querySingleLog("spResvCrearConfiguracionOta", params, scalarIntMapper, usuario, true, true);
    }

    public Optional<Integer> spResvAgregarUnidadesConfiguracionOta(
            Integer idConfiguracionOta,
            List<Integer> unidades,
            String usuario
    ) {
        String json = JsonUtils.toJson(Map.of("unidades", unidades));
        Map<String, Object> params = Map.of(
                "id_configuracion_ota", idConfiguracionOta,
                "configuracion_json", json,
                "usuario", usuario
        );
        // De acuerdo con las instrucciones del usuario, no se loggea (sin log)
        return spExecutor.querySingle("spResvAgregarUnidadesConfiguracionOta", params, scalarIntMapper);
    }

    public Optional<Integer> spResvEliminarUnidadesConfiguracionOta(
            Integer idConfiguracionOta,
            List<Integer> unidades,
            String usuario
    ) {
        String json = JsonUtils.toJson(Map.of("unidades", unidades));
        Map<String, Object> params = Map.of(
                "id_configuracion_ota", idConfiguracionOta,
                "configuracion_json", json,
                "usuario", usuario
        );
        // De acuerdo con las instrucciones del usuario, no se loggea (sin log)
        return spExecutor.querySingle("spResvEliminarUnidadesConfiguracionOta", params, scalarIntMapper);
    }

    public Optional<Integer> spResvDesactivarConfiguracionOta(
            Integer idConfiguracionOta,
            String usuario
    ) {
        Map<String, Object> params = Map.of(
                "id_configuracion_ota", idConfiguracionOta,
                "usuario", usuario
        );
        // De acuerdo con las instrucciones del usuario, no se loggea (sin log)
        return spExecutor.querySingle("spResvDesactivarConfiguracionOta", params, scalarIntMapper);
    }

    public List<ConfiguracionOtaResponse> spResvObtenerConfiguracionesOtas() {
        return spExecutor.queryList("spResvObtenerConfiguracionesOtas", Map.of(), configuracionOtaMapper);
    }

    public List<UnidadOtaResponse> spResvObtenerUnidadesConfiguracionOta(Integer idConfiguracionOta) {
        Map<String, Object> params = Map.of(
                "id_configuracion_ota", idConfiguracionOta
        );
        return spExecutor.queryList("spResvObtenerUnidadesConfiguracionOta", params, unidadOtaMapper);
    }

    public List<UnidadOtaResponse> spResvObtenerUnidadesDisponiblesParaOta(
            Integer idConfiguracionOta,
            java.time.LocalDate fechaInicio,
            java.time.LocalDate fechaFin
    ) {
        Map<String, Object> params = Map.of(
                "id_configuracion_ota", idConfiguracionOta,
                "fecha_inicio", fechaInicio,
                "fecha_fin", fechaFin
        );
        return spExecutor.queryList("spResvObtenerUnidadesDisponiblesParaOta", params, unidadOtaMapper);
    }

    public List<UnidadOtaResponse> spResvBuscarDisponibilidadUnidadesOta(
            Integer idDesarrollo,
            Integer tipoUnidad,
            java.time.LocalDate fechaInicio,
            java.time.LocalDate fechaFin
    ) {
        Map<String, Object> params = Map.of(
                "id_desarrollo", idDesarrollo,
                "tipo_unidad", tipoUnidad,
                "fecha_inicio", fechaInicio,
                "fecha_fin", fechaFin
        );
        return spExecutor.queryList("spResvBuscarDisponibilidadUnidadesOta", params, unidadOtaMapper);
    }

    public Optional<GenerarReservacionOtaResponse> spResvGeneraReservacionOta(
            Integer idOta,
            Integer idDesarrollo,
            String codigoVoucherOta,
            BigDecimal montoTarifaOta,
            String rsvMembresia,
            String nombreReservacion,
            String correoElectronico,
            String telefono,
            java.time.LocalDate fechaInicio,
            java.time.LocalDate fechaFin,
            Integer tipoUnidad,
            Integer idUnidad,
            Integer numeroSocios,
            String peticionEspecial,
            String usuario
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("id_ota", idOta);
        params.put("id_desarrollo", idDesarrollo);
        params.put("codigo_voucher_ota", codigoVoucherOta);
        params.put("monto_tarifa_ota", montoTarifaOta);
        params.put("rsv_membresia", rsvMembresia);
        params.put("nombre_reservacion", nombreReservacion);
        params.put("correo_electronico", correoElectronico);
        params.put("telefono", telefono);
        params.put("fecha_inicio", fechaInicio);
        params.put("fecha_fin", fechaFin);
        params.put("tipo_unidad", tipoUnidad);
        params.put("id_unidad", idUnidad);
        params.put("numero_socios", numeroSocios);
        params.put("peticion_especial", peticionEspecial);
        params.put("usuario", usuario);

        return spExecutor.querySingleLog("spResvGeneraReservacionOta", params, generarReservacionMapper, usuario, true, true);
    }
}
