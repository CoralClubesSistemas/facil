package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.cobranza.dto.request.GuardarCuponRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponesCatalogoElementoResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CuponesRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<CuponesCatalogoElementoResponse> catalogoElementoMapper = (rs, rowNum) ->
            new CuponesCatalogoElementoResponse(
                    rs.getString("clave"),
                    rs.getString("nombre"),
                    rs.getString("descripcion"),
                    rs.getString("tipo")
            );

    private final RowMapper<SelectGenerico<Integer>> selectGenericoMapper = (rs, rowNum) ->
            new SelectGenerico<>(rs.getInt("value"), rs.getString("label"));

    public List<CuponesCatalogoElementoResponse> spCuponesCatalogoCondiciones() {
        return spExecutor.queryList("spCuponesCatalogoCondiciones", Collections.emptyMap(), catalogoElementoMapper);
    }

    public List<CuponesCatalogoElementoResponse> spCuponesCatalogoBeneficios() {
        return spExecutor.queryList("spCuponesCatalogoBeneficios", Collections.emptyMap(), catalogoElementoMapper);
    }

    public List<SelectGenerico<Integer>> spCuponesCatalogoOrigenes() {
        return spExecutor.queryList("spCuponesCatalogoOrigenes", Collections.emptyMap(), selectGenericoMapper);
    }

    public List<SelectGenerico<Integer>> spCuponesCatalogoDestinos() {
        return spExecutor.queryList("spCuponesCatalogoDestinos", Collections.emptyMap(), selectGenericoMapper);
    }

    public Optional<Integer> spCuponesGuardarCupon(GuardarCuponRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", request.id());
        params.put("nombre", request.nombre());
        params.put("descripcion", request.descripcion());
        params.put("origene", request.origene());
        params.put("destino", request.destino());
        params.put("inicio_vigencia", request.inicioVigencia());
        params.put("fin_vigencia", request.finVigencia());
        params.put("es_transferible", request.esTransferible());
        params.put("formato", request.formato());
        params.put("nomenclatura", request.nomenclatura());
        params.put("beneficios", request.beneficios());
        params.put("condiciones", request.condiciones());

        return spExecutor.querySingle("spCuponesGuardarCupon", params, (rs, rowNum) -> rs.getInt(1));
    }
}
