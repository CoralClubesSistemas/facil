package com.coralclubes.facil.modules.reservaciones.repository;

import com.coralclubes.facil.modules.reservaciones.dto.request.FiltroConsultaGeneral;
import com.coralclubes.facil.modules.reservaciones.dto.response.ReservacionHistoricaDto;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ConsultaGeneralRepository {

    private final StoredProcedureExecutor spExecutor;

    public ConsultaGeneralRepository(StoredProcedureExecutor spExecutor) {
        this.spExecutor = spExecutor;
    }

    private final RowMapper<ReservacionHistoricaDto> reservacionHistoricaMapper = (rs, rowNum) -> ReservacionHistoricaDto.builder()
            .totalRegistros(rs.getInt("TotalRegistros"))
            .membresia(rs.getString("Membresia"))
            .consecutivo(rs.getInt("Consecutivo"))
            .nombreDesarrollo(rs.getString("NombreDesarrollo"))
            .nombreHuesped(rs.getString("NombreHuesped"))
            .tipoUnidad(rs.getString("TipoUnidad"))
            .numeroHabitacion(rs.getString("NumeroHabitacion"))
            .fechaEntrada(rs.getDate("FechaEntrada").toLocalDate())
            .fechaSalida(rs.getDate("FechaSalida").toLocalDate())
            .fechaRegistro(rs.getTimestamp("FechaRegistro").toLocalDateTime())
            .estatusClave(rs.getString("EstatusClave"))
            .estatusDescripcion(rs.getString("EstatusDescripcion"))
            .importeTotal(rs.getBigDecimal("ImporteTotal"))
            .importePendiente(rs.getBigDecimal("ImportePendiente"))
            .build();

    public List<ReservacionHistoricaDto> consultarHistoricoReservaciones(FiltroConsultaGeneral filtro) {
        Map<String, Object> params = new HashMap<>();
        params.put("DesarrolloId", filtro.desarrolloId());
        params.put("FechaInicio", filtro.fechaInicio());
        params.put("FechaFin", filtro.fechaFin());
        params.put("TipoFecha", filtro.tipoFecha());
        params.put("EstatusClave", filtro.estatusClave());
        params.put("Busqueda", filtro.busqueda());
        params.put("PageNumber", filtro.pageNumber());
        params.put("PageSize", filtro.pageSize());

        return spExecutor.queryList("spResvConsultaGeneralReservaciones", params, reservacionHistoricaMapper);
    }
}