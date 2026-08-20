package com.coralclubes.facil.modules.reportes.repository;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReportesCatalogoRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<SelectGenerico<Integer>> selectIntMapper = (rs, rowNum) ->
            new SelectGenerico<>(rs.getInt(1), rs.getString(2));

    private final RowMapper<SelectGenerico<String>> selectStringMapper = (rs, rowNum) ->
            new SelectGenerico<>(rs.getString(1), rs.getString(2));

    private <T> List<SelectGenerico<T>> ejecutarSp(String spName, RowMapper<SelectGenerico<T>> mapper) {
        return spExecutor.queryList(spName, java.util.Map.of(), mapper);
    }

    public List<SelectGenerico<Integer>> catalogoDesarrollos() {
        return ejecutarSp("spRepoCatalogoDesarrollos", selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoCartera() {
        return ejecutarSp("spRepoCatalogoCartera", selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoClasificacionMembresia() {
        return ejecutarSp("spRepoCatalogoClasificacionMembresia", selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoEmpleados() {
        return ejecutarSp("spRepoCatalogoEmpleados", selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoEstados() {
        return ejecutarSp("spRepoCatalogoEstados", selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoEstatusRecibos() {
        return ejecutarSp("spRepoCatalogoEstatusRecibos", selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoEstatusUnidades() {
        return ejecutarSp("spRepoCatalogoEstatusUnidades", selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoLocaciones() {
        return ejecutarSp("spRepoCatalogoLocaciones", selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoPromotores() {
        return ejecutarSp("spRepoCatalogoPromotores", selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoSeriesRecibos() {
        return ejecutarSp("spRepoCatalogoSeriesRecibos", selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoTipoCupones() {
        return ejecutarSp("spRepoCatalogoTipoCupones", selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoTiposMovimientos() {
        return ejecutarSp("spRepoCatalogoTiposMovimientos", selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoTiposProductos() {
        return ejecutarSp("spRepoCatalogoTiposProductos", selectIntMapper);
    }

    public List<SelectGenerico<String>> catalogoUsuarios() {
        return ejecutarSp("spRepoCatalogoUsuarios", selectStringMapper);
    }

    public List<SelectGenerico<Integer>> catalogoEstatusReservaciones() {
        return ejecutarSp("spRepoCatalogoEstatusReservaciones", selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoEstatusMembresia() {
        return ejecutarSp("spRepoCatalogoEstatusMembresias", selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoTiposMembresias() {
        return ejecutarSp("spRepoCatalogoTiposMembresias", selectIntMapper);
    }
}
