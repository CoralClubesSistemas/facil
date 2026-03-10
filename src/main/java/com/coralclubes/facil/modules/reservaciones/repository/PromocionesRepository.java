package com.coralclubes.facil.modules.reservaciones.repository;

import com.coralclubes.facil.modules.reservaciones.dto.projection.PromocionListProjection;
import com.coralclubes.facil.modules.reservaciones.dto.request.ConsumoOfertaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.PromocionIntegralRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.OpcionPagoPuntosDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.Promocion;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.utils.json.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class PromocionesRepository {
    private final StoredProcedureExecutor spExecutor;
    private final ObjectMapper objectMapper;

    // Mapper simple para extraer el ID devuelto por el SP de inserción/eliminación
    private final RowMapper<Integer> scalarIntMapper = (rs, rowNum) -> rs.getInt(1);

    private final RowMapper<PromocionListProjection> listMapper = (rs, rowNum) -> PromocionListProjection.builder()
            .idPromocion(rs.getInt("ID_PROMOCION"))
            .nombrePromocion(rs.getString("NOMBRE_PROMOCION"))
            .descripcionPromocion(rs.getString("DESCRIPCION_PROMOCION"))
            .codigoPromocion(rs.getString("CODIGO_PROMOCION"))
            .stockTotal(rs.getInt("STOCK_TOTAL"))
            .stockDisponible(rs.getInt("STOCK_DISPONIBLE"))
            .fechaInicio(rs.getTimestamp("FECHA_INICIO") != null ? rs.getTimestamp("FECHA_INICIO").toLocalDateTime() : null)
            .fechaFin(rs.getTimestamp("FECHA_FIN") != null ? rs.getTimestamp("FECHA_FIN").toLocalDateTime() : null)
            .esPrivada(rs.getBoolean("ES_PRIVADA"))
            .esGlobal(rs.getBoolean("ES_GLOBAL"))
            .fechaVisible(rs.getTimestamp("FECHA_VISIBLE") != null ? rs.getTimestamp("FECHA_VISIBLE").toLocalDateTime() : null)
            .uuidImagen(rs.getString("UUID_IMAGEN") != null ? UUID.fromString(rs.getString("UUID_IMAGEN")) : null)
            .build();

    // =========================================================================
    // ADMINISTRACIÓN (CREAR / EDITAR / ELIMINAR)
    // =========================================================================

    public List<PromocionListProjection> spResvObtenerPromociones() {
        Map<String, Object> params = new HashMap<>();
        return spExecutor.queryList("spResvObtenerPromociones", params, listMapper);
    }

    public Optional<Integer> spResvGuardarPromocionIntegral(PromocionIntegralRequest request, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("PayloadJson", JsonUtils.toJson(request));
        params.put("Usuario", usuario);

        return spExecutor.querySingleLog("spResvGuardarPromocionIntegral", params, scalarIntMapper, usuario, false, true);
    }

    public boolean spResvEliminarPromocion(Integer idPromocion, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdPromocion", idPromocion);
        params.put("Usuario", usuario);

        Optional<Integer> result = spExecutor.querySingleLog("spResvEliminarPromocion", params, scalarIntMapper, usuario, true, true);
        return result.isPresent() && result.get() > 0;
    }

    // =========================================================================
    // OPERACIÓN (VALIDAR Y CONSUMIR)
    // =========================================================================

    public Optional<Promocion> spResvObtenerPromocionPorCodigo(String codigo) {
        Map<String, Object> params = new HashMap<>();
        params.put("Codigo", codigo);

        // El SP devuelve un string JSON directo (WITHOUT_ARRAY_WRAPPER).
        RowMapper<String> jsonMapper = (rs, rowNum) -> rs.getString(1);

        List<String> results = spExecutor.queryList("spResvObtenerPromocionPorCodigo", params, jsonMapper);

        if (results.isEmpty() || results.getFirst() == null) {
            return Optional.empty();
        }

        try {
            // Deserializamos el JSON
            return Optional.ofNullable(objectMapper.readValue(results.getFirst(), Promocion.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Integer> spResvDetallarConsumoOferta(ConsumoOfertaRequest request, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", request.membresia());
        params.put("Consecutivo", request.consecutivo());
        params.put("OfertaId", request.ofertaId());
        params.put("Usuario", usuario);

        return spExecutor.querySingleLog("spResvDetallarConsumoOferta", params, scalarIntMapper, usuario, true, true);
    }

    public boolean spResvEnlazarImagenPromocion(Integer idPromocion, UUID uuidImagen, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdPromocion", idPromocion);
        params.put("UuidImagen", uuidImagen != null ? uuidImagen.toString() : null);
        params.put("Usuario", usuario);

        Optional<Integer> result = spExecutor.querySingle("spResvEnlazarImagenPromocion", params, scalarIntMapper);
        return result.isPresent() && result.get() > 0;
    }

    public Optional<UUID> spResvObtenerUuidImagenPromocion(Integer idPromocion) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdPromocion", idPromocion);

        RowMapper<String> uuidMapper = (rs, rowNum) -> rs.getString(1);
        List<String> results = spExecutor.queryList("spResvObtenerUuidImagenPromocion", params, uuidMapper);

        if (results.isEmpty() || results.getFirst() == null) {
            return Optional.empty();
        }

        return Optional.of(UUID.fromString(results.getFirst()));
    }
}