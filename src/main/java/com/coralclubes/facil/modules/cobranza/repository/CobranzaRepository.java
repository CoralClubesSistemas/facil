package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.facil.modules.cobranza.dto.response.FinalizarOrdenCobranzaResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.FormaPagoDto;
import com.coralclubes.facil.modules.cobranza.dto.response.GenerarOrdenCobranzaResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CobranzaRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<GenerarOrdenCobranzaResponse> generarOrdenCobranzaMapper = (rs, rowNum) ->
            new GenerarOrdenCobranzaResponse(
                    rs.getInt("numeroOrden"),
                    rs.getInt("desarrolloId"),
                    rs.getObject("ordenUuid") != null ? UUID.fromString(rs.getString("ordenUuid")) : null
            );

    private final RowMapper<FinalizarOrdenCobranzaResponse> finalizarOrdenCobranzaMapper = (rs, rowNum) ->
            new FinalizarOrdenCobranzaResponse(
                    rs.getInt("numeroRecibo"),
                    rs.getInt("serieReciboId"),
                    rs.getBigDecimal("totalPagado") != null ? rs.getBigDecimal("totalPagado") : BigDecimal.ZERO
            );

    private final RowMapper<FormaPagoDto> formaPagoMapper = (rs, rowNum) ->
            new FormaPagoDto(
                    rs.getInt("id"),
                    rs.getString("clave"),
                    rs.getString("descripcion"),
                    rs.getString("icono"),
                    rs.getString("color")
            );

    private final RowMapper<String> jsonStringMapper = (rs, rowNum) -> rs.getString(1);

    private final RowMapper<UUID> uuidMapper = (rs, rowNum) ->
            UUID.fromString(rs.getString("RCD_UUID"));

    public Optional<GenerarOrdenCobranzaResponse> spCobranzaGenerarOrdenCobranza(
            String membresia,
            String usuario,
            String movimientosJson
    ) {
        Map<String, Object> params = Map.of(
                "Membresia", membresia,
                "Usuario", usuario,
                "MovimientosJSON", movimientosJson
        );

        return spExecutor.querySingle("spCobranzaGenerarOrdenCobranza", params, generarOrdenCobranzaMapper);
    }

    public Optional<FinalizarOrdenCobranzaResponse> spCobranzaFinalizarOrdenYGenerarRecibo(
            String ordenUuid,
            Integer tipoSerieRecibo,
            String usuario
    ) {
        return spExecutor.querySingle(
                "spCobranzaFinalizarOrdenYGenerarRecibo",
                Map.of(
                        "OrdenUuid", ordenUuid,
                        "TipoSerieRecibo", tipoSerieRecibo,
                        "Usuario", usuario
                ),
                finalizarOrdenCobranzaMapper
        );
    }

    public Optional<String> spFacilConsultarOrdenCobranzaJson(UUID ordenUuid) {
        return spExecutor.querySingle(
                "spFacilConsultarOrdenCobranzaJson",
                Map.of("OrdenUuid", ordenUuid),
                jsonStringMapper
        );
    }

    public Optional<String> spCobranzaObtenerDatosRecibo(Integer numeroRecibo, Integer serieReciboId) {
        return spExecutor.querySingle(
                "spCobranzaObtenerDatosRecibo",
                Map.of(
                        "NumeroRecibo", numeroRecibo,
                        "SerieReciboId", serieReciboId
                ),
                jsonStringMapper
        );
    }

    public List<FormaPagoDto> spCobranzaCatalogoFormasDePago() {
        return spExecutor.queryList("spCobranzaCatalogoFormasDePago", Collections.emptyMap(), formaPagoMapper);
    }

    // Modificación en CobranzaRepository.java
    public Optional<UUID> spCobranzaActualizarMetadatosDigitales(
            Integer numeroRecibo,
            Integer serieReciboId,
            String fileId,
            String cadenaSeguridad,
            String usuario
    ) {
        return spExecutor.querySingle(
                "spCobranzaActualizarMetadatosDigitales",
                Map.of(
                        "NumeroRecibo", numeroRecibo,
                        "SerieReciboId", serieReciboId,
                        "FileId", fileId,
                        "CadenaSeguridad", cadenaSeguridad,
                        "Usuario", usuario
                ),
                uuidMapper
        );
    }

    public void spCobranzaCancelarOrdenCobranzaSinPago(String uuid) {
        spExecutor.execute(
                "spCobranzaCancelarOrdenCobranzaSinPago",
                Map.of("OrdenUuid", uuid)
        );
    }
}
