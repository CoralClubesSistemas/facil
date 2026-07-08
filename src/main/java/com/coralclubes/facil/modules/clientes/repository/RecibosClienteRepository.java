package com.coralclubes.facil.modules.clientes.repository;

import com.coralclubes.facil.modules.clientes.dto.response.ReciboClienteDto;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class RecibosClienteRepository {

    private final StoredProcedureExecutor executor;

    private final RowMapper<ReciboClienteDto> reciboClienteRowMapper = (rs, rowNum) -> ReciboClienteDto.builder()
            .folioRecibo(rs.getString("folio_recibo"))
            .numeroRecibo(rs.getObject("numero_recibo") != null ? rs.getInt("numero_recibo") : null)
            .serieRecibo(rs.getObject("serie_recibo") != null ? rs.getInt("serie_recibo") : null)
            .build();

    public List<ReciboClienteDto> spCobranzaObtenerListadoRecibosMembresia(String membresia) {
        Map<String, Object> params = Map.of("membresia", membresia);
        return executor.queryList("spCobranzaObtenerListadoRecibosMembresia", params, reciboClienteRowMapper);
    }
}
