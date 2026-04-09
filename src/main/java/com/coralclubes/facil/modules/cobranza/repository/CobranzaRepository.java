package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.facil.modules.cobranza.dto.response.GenerarOrdenCobranzaResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

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
}
