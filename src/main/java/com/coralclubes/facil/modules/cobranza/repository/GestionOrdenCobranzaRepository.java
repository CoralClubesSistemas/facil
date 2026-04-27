package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.facil.modules.cobranza.dto.projection.LinkGestionResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GestionOrdenCobranzaRepository {
    private final StoredProcedureExecutor executor;

    private final RowMapper<LinkGestionResponse> linkGestionMapper = (rs, rowNum) ->
            new LinkGestionResponse(
                    rs.getString("tokenUuid"),
                    rs.getInt("idGestion"),
                    rs.getDate("fechaVigencia") != null ? rs.getDate("fechaVigencia").toLocalDate() : null
            );

    public Optional<LinkGestionResponse> spCobranzaGenerarLinkPagoPorOrden(
            String ordenUuid,
            String usuario,
            LocalDate fechaInicioVigencia,
            LocalDate fechaFinVigencia,
            Boolean habilitarMeses
    ) {
        return executor.querySingle(
                "spCobranzaGenerarLinkPagoPorOrden",
                Map.of(
                        "ordenUuid", ordenUuid,
                        "usuario", usuario,
                        "fechaInicioVigencia", fechaInicioVigencia,
                        "fechaFinVigencia", fechaFinVigencia,
                        "habilitarMeses", habilitarMeses
                ),
                linkGestionMapper
        );
    }
}
