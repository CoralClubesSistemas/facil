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

    public List<SelectGenerico<Integer>> catalogoDesarrollos() {
        return spExecutor.queryList("spRepoCatalogoDesarrollos", java.util.Map.of(), selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoCartera() {
        return spExecutor.queryList("spRepoCatalogoCartera", java.util.Map.of(), selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoClasificacionMembresia() {
        return spExecutor.queryList("spRepoCatalogoClasificacionMembresia", java.util.Map.of(), selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoEmpleados() {
        return spExecutor.queryList("spRepoCatalogoEmpleados", java.util.Map.of(), selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoEstados() {
        return spExecutor.queryList("spRepoCatalogoEstados", java.util.Map.of(), selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoEstatusRecibos() {
        return spExecutor.queryList("spRepoCatalogoEstatusRecibos", java.util.Map.of(), selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoEstatusUnidades() {
        return spExecutor.queryList("spRepoCatalogoEstatusUnidades", java.util.Map.of(), selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoLocaciones() {
        return spExecutor.queryList("spRepoCatalogoLocaciones", java.util.Map.of(), selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoPromotores() {
        return spExecutor.queryList("spRepoCatalogoPromotores", java.util.Map.of(), selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoSeriesRecibos() {
        return spExecutor.queryList("spRepoCatalogoSeriesRecibos", java.util.Map.of(), selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoTipoCupones() {
        return spExecutor.queryList("spRepoCatalogoTipoCupones", java.util.Map.of(), selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoTiposMovimientos() {
        return spExecutor.queryList("spRepoCatalogoTiposMovimientos", java.util.Map.of(), selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoTiposProductos() {
        return spExecutor.queryList("spRepoCatalogoTiposProductos", java.util.Map.of(), selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoUsuarios() {
        return spExecutor.queryList("spRepoCatalogoUsuarios", java.util.Map.of(), selectIntMapper);
    }

    public List<SelectGenerico<Integer>> catalogoEstatusReservaciones() {
        return spExecutor.queryList("spRepoCatalogoEstatusReservaciones", java.util.Map.of(), selectIntMapper);
    }
}
