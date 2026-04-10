package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.facil.modules.cobranza.dto.response.FormaPagoDto;
import com.coralclubes.facil.modules.cobranza.dto.response.GenerarOrdenCobranzaResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

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

    private final RowMapper<FormaPagoDto> formaPagoMapper = (rs, rowNum) ->
            new FormaPagoDto(
                    rs.getInt("id"),
                    rs.getString("clave"),
                    rs.getString("descripcion"),
                    rs.getString("icono"),
                    rs.getString("color")
            );

    private final RowMapper<String> jsonStringMapper = (rs, rowNum) -> rs.getString(1);

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

    public Optional<String> spFacilConsultarOrdenCobranzaJson(UUID ordenUuid) {
        return spExecutor.querySingle(
                "spFacilConsultarOrdenCobranzaJson",
                Map.of("OrdenUuid", ordenUuid),
                jsonStringMapper
        );
    }

    public List<FormaPagoDto> spCobranzaCatalogoFormasDePago() {
        return spExecutor.queryList("spCobranzaCatalogoFormasDePago", Collections.emptyMap(), formaPagoMapper);
    }
}
