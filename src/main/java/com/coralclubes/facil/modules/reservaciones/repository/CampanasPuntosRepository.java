package com.coralclubes.facil.modules.reservaciones.repository;

import com.coralclubes.facil.modules.reservaciones.dto.request.CampanaPuntosRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.CampanaPuntosResponse;
import com.coralclubes.facil.modules.reservaciones.dto.response.OpcionPagoPuntosDto;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.utils.json.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class CampanasPuntosRepository {

    private final StoredProcedureExecutor spExecutor;
    private final RowMapper<Integer> scalarIntMapper = (rs, rowNum) -> rs.getInt(1);

    private final RowMapper<CampanaPuntosResponse> listMapper = (rs, rowNum) -> {
        String tabuladorJsonStr = rs.getString("tabuladorDetalleJson");

        List<CampanaPuntosResponse.TabuladorPuntosResponse> tabulador = null;
        if (tabuladorJsonStr != null) {
            tabulador = JsonUtils.fromJson(tabuladorJsonStr, new TypeReference<>() {
            });
        }

        return CampanaPuntosResponse.builder()
                .idPromocion(rs.getInt("idPromocion"))
                .nombre(rs.getString("nombre"))
                .descripcion(rs.getString("descripcion"))
                .imagenUuid(rs.getString("imagenUuid"))
                .fechaInicio(rs.getDate("fechaInicio").toLocalDate())
                .fechaFin(rs.getDate("fechaFin").toLocalDate())
                .fechaVisibilidad(rs.getDate("fechaVisibilidad").toLocalDate())
                .clasificacionId(rs.getInt("clasificacionId"))
                .clasificacionDescripcion(rs.getString("clasificacionDescripcion"))
                .temporadaId(rs.getInt("temporadaId"))
                .temporadaDescripcion(rs.getString("temporadaDescripcion"))
                .tabuladorDetalleJson(tabulador)
                .build();
    };

    public List<CampanaPuntosResponse> obtenerCampanasPuntos() {
        // Tu SP ya no recibe parámetros, filtra por RRP_ACTIVO = 1 internamente
        Map<String, Object> params = new HashMap<>();
        return spExecutor.queryList("spResvObtenerPromocionesPuntos", params, listMapper);
    }

    public Optional<Integer> guardarCampanaPuntos(CampanaPuntosRequest request, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("RRP_ID", request.idPromocion());
        params.put("Nombre", request.nombre());
        params.put("Descripcion", request.descripcion());
        params.put("ImagenUUID", request.imagenUuid());
        params.put("FechaInicio", request.fechaInicio());
        params.put("FechaFin", request.fechaFin());
        params.put("FechaVisibilidad", request.fechaVisibilidad());
        params.put("Temporada", request.temporadaId());
        params.put("Usuario", usuario);
        params.put("TabuladorJson", JsonUtils.toJson(request.tabulador()));

        return spExecutor.querySingleLog("spResvGuardarPromocionPuntos", params, scalarIntMapper, usuario, true, true);
    }

    public void eliminarCampanaPuntos(Integer idPromocion, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("RRP_ID", idPromocion);
        params.put("Usuario", usuario);

        spExecutor.querySingleLog("spResvEliminarPromocionPuntos", params, scalarIntMapper, usuario, true, true);
    }

    public List<OpcionPagoPuntosDto> evaluarPromocionesPuntosCarrito(UUID groupId) {
        Map<String, Object> params = new HashMap<>();
        params.put("GroupId", groupId.toString());

        RowMapper<OpcionPagoPuntosDto> mapper = (rs, rowNum) -> OpcionPagoPuntosDto.builder()
                .rrtId(rs.getInt("rrtId"))
                .rhdtId(rs.getInt("rhdtId"))
                .promocionId(rs.getInt("promocionId"))
                .nombrePromocion(rs.getString("nombrePromocion"))
                .costoTotalPuntos(rs.getInt("costoTotalPuntos"))
                .build();

        return spExecutor.queryList("spResvEvaluarPromocionesPuntosCarrito", params, mapper);
    }
}