package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.facil.modules.cobranza.dto.projection.GenerarGestionCobranzaResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class GestionCobranzaRepository {
    private final StoredProcedureExecutor executor;

    private final RowMapper<GenerarGestionCobranzaResponse> gestionCobranzaMapper = (rs, rowNum) ->
            new GenerarGestionCobranzaResponse(
                    rs.getObject("tokenPagoEnLinea") != null ? UUID.fromString(rs.getString("tokenPagoEnLinea")) : null,
                    rs.getInt("idGestionCobranza")
            );

    public Optional<GenerarGestionCobranzaResponse> spCobranzaGenerarGestionCobranza(
            String membresia,
            String usuario,
            LocalDateTime fechaInicioVigencia,
            LocalDateTime fechaFinVigencia,
            Boolean habilitarMeses,
            String movimientosJson
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("Usuario", usuario);
        params.put("FechaInicioVigencia", fechaInicioVigencia);
        params.put("FechaFinVigencia", fechaFinVigencia);
        params.put("HabilitarMeses", habilitarMeses);
        params.put("MovimientosJSON", movimientosJson);

        return executor.querySingle("spCobranzaGenerarGestionCobranza", params, gestionCobranzaMapper);
    }
}

