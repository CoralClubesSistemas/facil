package com.coralclubes.facil.modules.clientes.repository;

import com.coralclubes.facil.modules.clientes.dto.response.MembresiaDatosDto;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MembresiaRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<MembresiaDatosDto> rowMapper = (rs, rowNum) -> MembresiaDatosDto.builder()
            .membresia(rs.getString("membresia"))
            .idDesarrollo(rs.getObject("idDesarrollo") != null ? rs.getInt("idDesarrollo") : null)
            .desarrollo(rs.getString("desarrollo"))
            .idEstatusMembresia(rs.getObject("idEstatusMembresia") != null ? rs.getInt("idEstatusMembresia") : null)
            .estatusMembresia(rs.getString("estatusMembresia"))
            .idPuntoDeVenta(rs.getObject("idPuntoDeVenta") != null ? rs.getInt("idPuntoDeVenta") : null)
            .puntoDeVenta(rs.getString("puntoDeVenta"))
            .idTipoMembresia(rs.getObject("idTipoMembresia") != null ? rs.getInt("idTipoMembresia") : null)
            .tipoMembresia(rs.getString("tipoMembresia"))
            .idClasificacionMembresia(rs.getObject("idClasificacionMembresia") != null ? rs.getInt("idClasificacionMembresia") : null)
            .clasificacionMembresia(rs.getString("clasificacionMembresia"))
            .fechaVenta(rs.getTimestamp("fechaVenta") != null ? rs.getTimestamp("fechaVenta").toLocalDateTime() : null)
            .numeroPlan(rs.getObject("numeroPlan") != null ? rs.getInt("numeroPlan") : null)
            .precioPlan(rs.getBigDecimal("precioPlan"))
            .descuento(rs.getBigDecimal("descuento"))
            .montoNeto(rs.getBigDecimal("montoNeto"))
            .enganche(rs.getBigDecimal("enganche"))
            .intereses(rs.getBigDecimal("intereses"))
            .saldo(rs.getBigDecimal("saldo"))
            .numeroMensualidades(rs.getObject("numeroMensualidades") != null ? rs.getInt("numeroMensualidades") : null)
            .importeMensualidades(rs.getBigDecimal("importeMensualidades"))
            .inicioMensualidades(rs.getTimestamp("inicioMensualidades") != null ? rs.getTimestamp("inicioMensualidades").toLocalDateTime() : null)
            .montoProcesable(rs.getBigDecimal("montoProcesable"))
            .estatusProcesable(rs.getObject("estatusProcesable") != null ? rs.getInt("estatusProcesable") : null)
            .descripcionEstatusProcesable(rs.getString("descripcionEstatusProcesable"))
            .fechaProcesable(rs.getTimestamp("fechaProcesable") != null ? rs.getTimestamp("fechaProcesable").toLocalDateTime() : null)
            .build();

    public Optional<MembresiaDatosDto> spCobranzaOntenerDatosMembresia(String membresia, Integer plan) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("Plan", plan);

        return spExecutor.querySingle(
                "spCobranzaOntenerDatosMembresia",
                params,
                rowMapper
        );
    }
}
