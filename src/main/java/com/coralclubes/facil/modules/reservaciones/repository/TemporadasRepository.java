package com.coralclubes.facil.modules.reservaciones.repository;

import com.coralclubes.facil.modules.reservaciones.dto.request.TemporadaMasivaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.TemporadaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.TemporadaDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.TemporadaFechaResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TemporadasRepository {

    private final StoredProcedureExecutor spExecutor;

    // Mapper de SQL a Java
    private final RowMapper<TemporadaDto> temporadaMapper = (rs, rowNum) -> TemporadaDto.builder()
            .idTemporadaFecha(rs.getInt("ID_TEMPORADA_FECHA"))
            .idDesarrollo(rs.getObject("ID_DESARROLLO") != null ? rs.getInt("ID_DESARROLLO") : null)
            .nombreDesarrollo(rs.getString("NOMBRE_DESARROLLO"))
            .idTipoTemporada(rs.getInt("ID_TIPO_TEMPORADA"))
            .nombreTemporada(rs.getString("NOMBRE_TEMPORADA"))
            .fechaInicio(rs.getDate("FECHA_INICIO").toLocalDate()) // Convertimos java.sql.Date a LocalDate
            .fechaFinal(rs.getDate("FECHA_FINAL").toLocalDate())
            .build();

    private final RowMapper<Integer> scalarIntMapper = (rs, rowNum) -> rs.getInt(1);
    private final RowMapper<Boolean> scalarBooleanMapper = (rs, rowNum) -> rs.getBoolean(1);

    private final RowMapper<TemporadaFechaResponse> temporadaFechaMapper = (rs, rowNum) -> TemporadaFechaResponse.builder()
            .idDesarrollo(rs.getInt("ID_DESARROLLO"))
            .nombreDesarrollo(rs.getString("NOMBRE_DESARROLLO"))
            .idTemporada(rs.getInt("ID_TIPO_TEMPORADA"))
            .nombreTemporada(rs.getString("NOMBRE_TEMPORADA"))
            .fechaInicio(rs.getDate("FECHA_INICIO").toLocalDate())
            .fechaFinal(rs.getDate("FECHA_FINAL").toLocalDate())
            .build();

    // =========================================================================
    // LECTURA
    // =========================================================================

    @Cacheable(value = "temporadas_reservaciones", key = "(#idDesarrollo != null ? #idDesarrollo : 0) + '-' + (#anio != null ? #anio : 'todos')")
    public List<TemporadaDto> spResvObtenerTemporadasReservaciones(Integer idDesarrollo, Integer anio) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdDesarrollo", idDesarrollo != null ? idDesarrollo : 0);
        params.put("Anio", anio); // Puede ser null

        return spExecutor.queryList("spResvObtenerTemporadasReservaciones", params, temporadaMapper);
    }

    // =========================================================================
    // ESCRITURA
    // =========================================================================

    @Caching(evict = {
            @CacheEvict(value = "temporadas_reservaciones", allEntries = true),
            @CacheEvict(value = "temporadas_fecha", allEntries = true)
    })
    public Optional<Integer> spResvGuardarTemporadaReservacion(TemporadaRequest request, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdTemporadaFecha", request.idTemporadaFecha());
        params.put("IdsDesarrollosJson", JsonUtils.toJson(request.idsDesarrollos()));
        params.put("IdTipoTemporada", request.idTipoTemporada());
        params.put("FechaInicio", request.fechaInicio());
        params.put("FechaFin", request.fechaFinal());
        params.put("Usuario", usuario);

        // Pasamos throwException = true para que el THROW de SQL Server llegue al GlobalExceptionHandler
        return spExecutor.querySingleLog("spResvGuardarTemporadaReservacion", params, scalarIntMapper, usuario, false, true);
    }

    @Caching(evict = {
            @CacheEvict(value = "temporadas_reservaciones", allEntries = true),
            @CacheEvict(value = "temporadas_fecha", allEntries = true)
    })
    public void spResvEliminarTemporadaReservacion(Integer idTemporadaFecha, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdTemporadaFecha", idTemporadaFecha);

        spExecutor.querySingleLog("spResvEliminarTemporadaReservacion", params, scalarBooleanMapper, usuario, true, true);
    }

    @Cacheable(value = "temporadas_fecha", key = "(#idDesarrollo != null ? #idDesarrollo : 0) + '-' + #fecha")
    public List<TemporadaFechaResponse> spResvObtenerTemporadasFecha(Integer idDesarrollo, LocalDate fecha) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdDesarrollo", idDesarrollo != null ? idDesarrollo : 0);
        params.put("Fecha", fecha);


        return spExecutor.queryList("spResvObtenerTemporadasFecha", params, temporadaFechaMapper);
    }

    @Caching(evict = {
            @CacheEvict(value = "temporadas_reservaciones", allEntries = true),
            @CacheEvict(value = "temporadas_fecha", allEntries = true)
    })
    public Optional<Integer> spResvGuardarTemporadasMasivas(List<TemporadaMasivaRequest> temporadas, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("TemporadasJson", JsonUtils.toJson(temporadas)); // Convierte la lista a JSON
        params.put("Usuario", usuario);

        return spExecutor.querySingleLog("spResvGuardarTemporadasMasivas", params, scalarIntMapper, usuario, false, true);
    }
}