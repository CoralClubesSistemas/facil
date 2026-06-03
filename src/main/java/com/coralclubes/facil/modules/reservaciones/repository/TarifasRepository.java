package com.coralclubes.facil.modules.reservaciones.repository;

import com.coralclubes.facil.modules.reservaciones.dto.request.TarifaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.TarifaDto;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TarifasRepository {

    private final StoredProcedureExecutor spExecutor;
    private final RowMapper<Integer> scalarIntMapper = (rs, rowNum) -> rs.getInt(1);

    // Mapper de SQL a Java para el SP de Lectura
    private final RowMapper<TarifaDto> tarifaMapper = (rs, rowNum) -> TarifaDto.builder()
            .idTarifa(rs.getInt("ID_TARIFA"))
            .idDesarrollo(rs.getInt("ID_DESARROLLO"))
            .nombreDesarrollo(rs.getString("NOMBRE_DESARROLLO"))
            .idTipoHabitacion(rs.getObject("ID_TIPO_HABITACION") != null ? rs.getInt("ID_TIPO_HABITACION") : null)
            .nombreTipoHabitacion(rs.getString("NOMBRE_TIPO_HABITACION"))
            .idTipoAcceso(rs.getInt("ID_TIPO_ACCESO"))
            .nombreTipoAcceso(rs.getString("NOMBRE_TIPO_ACCESO"))
            .idTipoTemporada(rs.getInt("ID_TIPO_TEMPORADA"))
            .nombreTemporada(rs.getString("NOMBRE_TEMPORADA"))
            .idTipoTarifa(rs.getInt("ID_TIPO_TARIFA"))
            .nombreTipoTarifa(rs.getString("NOMBRE_TIPO_TARIFA"))
            .idTipoCalculo(rs.getInt("ID_TIPO_CALCULO"))
            .nombreTipoCalculo(rs.getString("NOMBRE_TIPO_CALCULO"))
            .capacidad(rs.getInt("CAPACIDAD"))
            .tipoUnidadLegacy(rs.getString("TIPO_UNIDAD_LEGACY"))
            .anioVigencia(rs.getInt("ANIO_VIGENCIA"))
            .costoNoche(rs.getBigDecimal("COSTO_NOCHE"))
            .puntos(rs.getInt("PUNTOS"))
            .costoPersonaExtra(rs.getBigDecimal("COSTO_PERSONA_EXTRA"))
            .incrementoInvitados(rs.getInt("INCREMENTO_INVITADOS"))
            .build();


    // =========================================================================
    // LECTURA
    // =========================================================================

    @Cacheable(value = "tarifas_reservaciones", key = "(#idDesarrollo != null ? #idDesarrollo : 0) + '-' + (#anio != null ? #anio : 'todos')")
    public List<TarifaDto> spResvObtenerTarifasReservaciones(Integer idDesarrollo, Integer anio) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdDesarrollo", idDesarrollo != null ? idDesarrollo : 0);
        params.put("Anio", anio); // Puede ser null, el SP pone el año actual

        return spExecutor.queryList("spResvObtenerTarifasReservaciones", params, tarifaMapper);
    }

    // =========================================================================
    // ESCRITURA Y ELIMINACIÓN
    // =========================================================================

    @CacheEvict(value = "tarifas_reservaciones", allEntries = true)
    public Optional<Integer> spResvGuardarTarifasReservaciones(List<TarifaRequest> tarifas, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("TarifasJson", JsonUtils.toJson(tarifas)); // Magia: Parseamos la lista a string JSON
        params.put("Usuario", usuario);

        return spExecutor.querySingleLog("spResvGuardarTarifasReservaciones", params, scalarIntMapper, usuario, false, true);
    }

    @CacheEvict(value = "tarifas_reservaciones", allEntries = true)
    public Optional<Integer> spResvEliminarTarifasReservaciones(List<Integer> idsTarifas, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdsJson", JsonUtils.toJson(idsTarifas)); // Se enviará algo como "[5, 6, 7]"
        params.put("Usuario", usuario);

        return spExecutor.querySingleLog("spResvEliminarTarifasReservaciones", params, scalarIntMapper, usuario, true, true);
    }
}