package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.facil.modules.cobranza.dto.response.BuscarRecibosResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class RecibosRepository {
    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<BuscarRecibosResponse> buscarRecibosRowMapper = (rs, rowNum) ->
            new BuscarRecibosResponse(
                    rs.getString("Membresia"),
                    rs.getInt("NumeroRecibo"),
                    rs.getInt("SerieReciboId"),
                    rs.getString("SerieReciboDescripcion"),
                    rs.getString("FolioRecibo"),
                    rs.getString("clienteNombre"),
                    rs.getTimestamp("FechaGeneracion") != null ? rs.getTimestamp("FechaGeneracion").toLocalDateTime() : null,
                    rs.getTimestamp("FechaPago") != null ? rs.getTimestamp("FechaPago").toLocalDateTime() : null,
                    rs.getBigDecimal("ImporteRecibo"),
                    rs.getString("Usuario"),
                    rs.getInt("EstatusReciboId"),
                    rs.getString("EstatusReciboDescripcion"),
                    rs.getInt("DesarrolloId"),
                    rs.getString("DesarrolloDescripcion"),
                    rs.getString("TipoMembresia")
            );

    /**
     * Busca recibos de cobranza con múltiples filtros opcionales.
     * @param folioRecibo Formato: numero-serieDescripcion
     * @param fechaGeneracionDe Fecha desde (ISO 8601)
     * @param fechaGeneracionA Fecha hasta (ISO 8601)
     * @param membresia Identificador de membresía
     * @param desarrolloId ID del desarrollo
     * @param usuario Código de usuario que generó el recibo
     * @param nombreSocio Búsqueda en nombre completo del cliente
     * @param terminacionTarjeta Últimos dígitos de tarjeta (si aplica)
     * @param filtrarPorEstatus 1 = solo Generado (684), 0 = múltiples estatus
     * @return Lista de recibos que cumplen los criterios
     */
    public List<BuscarRecibosResponse> spCobranzaBuscarRecibos(
            String folioRecibo,
            LocalDate fechaGeneracionDe,
            LocalDate fechaGeneracionA,
            String membresia,
            Integer desarrolloId,
            String usuario,
            String nombreSocio,
            String terminacionTarjeta,
            Boolean filtrarPorEstatus
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("FolioRecibo", folioRecibo);
        params.put("FechaGeneracionDe", fechaGeneracionDe);
        params.put("FechaGeneracionA", fechaGeneracionA);
        params.put("Membresia", membresia);
        params.put("DesarrolloId", desarrolloId);
        params.put("Usuario", usuario);
        params.put("NombreSocio", nombreSocio);
        params.put("TerminacionTarjeta", terminacionTarjeta);
        params.put("FiltrarPorEstatus", filtrarPorEstatus != null && filtrarPorEstatus ? 1 : 0);

        return spExecutor.queryList("spCobranzaBuscarRecibos", params, buscarRecibosRowMapper);
    }
}
