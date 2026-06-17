package com.coralclubes.facil.modules.reservaciones.repository;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.reservaciones.dto.response.CaracteristicaDto;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.coralclubes.facil.modules.reservaciones.dto.request.GuardarCaracteristicaRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CatalogosRepository {

    private final StoredProcedureExecutor spExecutor;

    // =========================================================================
    // MAPPERS
    // =========================================================================

    private final RowMapper<SelectGenerico<Integer>> selectGenericoMapper = (rs, rowNum) ->
            new SelectGenerico<>(rs.getInt(1), rs.getString(2));

    private final RowMapper<SelectGenerico<String>> selectStringMapper = (rs, rowNum) ->
            new SelectGenerico<>(rs.getString(1), rs.getString(2));

    private final RowMapper<CaracteristicaDto> caracteristicaMapper = (rs, rowNum) ->
            CaracteristicaDto.builder()
                    .idCaracteristica(rs.getInt("ID_CARACTERISTICA"))
                    .descripcion(rs.getString("DESCRIPCION"))
                    .nombre(rs.getString("NOMBRE"))
                    .icono(rs.getString("ICONO"))
                    .tabla(rs.getString("TABLA"))
                    .build();

    // =========================================================================
    // MÉTODOS 
    // =========================================================================

    public List<SelectGenerico<Integer>> spResvCatalogoHoteles(Integer idDesarrollo) {
        return spExecutor.queryList("spResvCatalogoHoteles", Map.of("ID_DESARROLLO", idDesarrollo), selectGenericoMapper);
    }

    public List<SelectGenerico<Integer>> spResvCatalogoTiposHabitaciones(Integer idDesarrollo) {
        return spExecutor.queryList("spResvCatalogoTiposHabitaciones", Map.of("ID_DESARROLLO", idDesarrollo), selectGenericoMapper);
    }

    public List<SelectGenerico<Integer>> spResvCatalogoDestinos(Integer idDestino) {
        return spExecutor.queryList("spResvCatalogoDestinos", Map.of("ID_DESTINO", idDestino), selectGenericoMapper);
    }

    public List<SelectGenerico<Integer>> spResvCatalogoTemporadas() {
        return spExecutor.queryList("spResvCatalogoTemporadas", Map.of(), selectGenericoMapper);
    }

    public List<SelectGenerico<Integer>> spResvCatalogoTiposAccesos() {
        return spExecutor.queryList("spResvCatalogoTiposAccesos", Map.of(), selectGenericoMapper);
    }

    public List<SelectGenerico<Integer>> spResvCatalogoTiposTarifas() {
        return spExecutor.queryList("spResvCatalogoTiposTarifas", Map.of(), selectGenericoMapper);
    }

    public List<SelectGenerico<Integer>> spResvCatalogoOrigenesReservas() {
        return spExecutor.queryList("spResvCatalogoOrigenesReservas", Map.of(), selectGenericoMapper);
    }

    public List<SelectGenerico<Integer>> spResvCatalogoPeriodoTarifa() {
        return spExecutor.queryList("spResvCatalogoPeriodoTarifa", Map.of(), selectGenericoMapper);
    }

    public List<SelectGenerico<Integer>> spResvCatalogoTiposUnidades(String idHoteles) {
        return spExecutor.queryList("spResvCatalogoTiposUnidades", Map.of("ID_HOTELES", idHoteles), selectGenericoMapper);
    }

    public List<SelectGenerico<String>> spResvCatalogoTiposPromociones() {
        return spExecutor.queryList("spResvCatalogoTiposPromociones", Map.of(), selectStringMapper);
    }

    public List<SelectGenerico<Integer>> spResvCatalogoTiposOferta() {
        return spExecutor.queryList("spResvCatalogoTiposOferta", Map.of(), selectGenericoMapper);
    }

    public List<SelectGenerico<String>> spResvCatalogoAccionObjetivo() {
        return spExecutor.queryList("spResvCatalogoAccionObjetivo", Map.of(), selectStringMapper);
    }

    public List<CaracteristicaDto> spResvObtenerCaracteristicasReservaciones() {
        return spExecutor.queryList("spResvObtenerCaracteristicasReservaciones", Map.of(), caracteristicaMapper);
    }

    public List<SelectGenerico<String>> spResvCatalogoTiposReglas() {
        return spExecutor.queryList("spResvCatalogoTiposReglas", Map.of(), selectStringMapper);
    }

    public List<SelectGenerico<String>> spResvCatalogoComparadores() {
        return spExecutor.queryList("spResvCatalogoComparadores", Map.of(), selectStringMapper);
    }

    public List<SelectGenerico<Integer>> spResvCatalogoCargosHabitacion(String membresia) {
        return spExecutor.queryList("spResvCatalogoCargosHabitacion", Map.of("Membresia", membresia), selectGenericoMapper);
    }

    public List<SelectGenerico<Integer>> spResvCatalogoOtas() {
        return spExecutor.queryList("spResvCatalogoOtas", Map.of(), selectGenericoMapper);
    }

    public List<SelectGenerico<Integer>> spResvCatalogoTipoCaracteristica() {
        return spExecutor.queryList("spResvCatalogoTipoCaracteristica", Map.of(), selectGenericoMapper);
    }

    public void spResvGuardarCaracteristica(GuardarCaracteristicaRequest request, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", request.id());
        params.put("nombre", request.nombre());
        params.put("descripcion", request.descripcion());
        params.put("icono", request.icono());
        params.put("lsv_tabla", request.lsvTabla());
        params.put("usuario", usuario);

        spExecutor.executeLog("spResvGuardarCaracteristica", params, usuario, false, true);
    }
}