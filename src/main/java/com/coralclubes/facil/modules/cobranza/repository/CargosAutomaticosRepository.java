package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.facil.modules.cobranza.dto.response.RechazoCAResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CargosAutomaticosRepository {

    private final StoredProcedureExecutor executor;

    private final RowMapper<RechazoCAResponse> rechazoMapper = (rs, rowNum) ->
            new RechazoCAResponse(
                    rs.getString("tarjeta"),
                    rs.getString("membresia"),
                    rs.getString("socio"),
                    rs.getTimestamp("fecha_rechazo") != null ? rs.getTimestamp("fecha_rechazo").toLocalDateTime() : null,
                    rs.getString("motivo_rechazo"),
                    rs.getString("concepto_rechazo"),
                    rs.getBigDecimal("importe_rechazo"),
                    rs.getBigDecimal("comision_cargo"),
                    rs.getTimestamp("fecha_registro") != null ? rs.getTimestamp("fecha_registro").toLocalDateTime() : null,
                    rs.getString("usuario_registro")
            );

    public List<RechazoCAResponse> spCobranzaObtenerRechazosCA(String membresia) {
        return executor.queryList("spCobranzaObtenerRechazosCA", Map.of(
                "Membresia", membresia
        ), rechazoMapper);
    }
}
