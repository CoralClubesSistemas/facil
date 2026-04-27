package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.facil.modules.cobranza.dto.request.EstadoCuentaAdeudoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.EstadoCuentaAdeudoDto;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class MovimientosClienteRepository {

	private final StoredProcedureExecutor executor;

	private final RowMapper<EstadoCuentaAdeudoDto> estadoCuentaAdeudoRowMapper = (rs, rowNum) -> EstadoCuentaAdeudoDto.builder()
			.id(rs.getInt("id"))
			.movimientoOriginalId(rs.getObject("movimientoOriginalId") != null ? rs.getInt("movimientoOriginalId") : null)
			.movimientoPadreId(rs.getObject("movimientoPadreId") != null ? rs.getInt("movimientoPadreId") : null)
			.fechaGeneracion(rs.getTimestamp("fechaGeneracion") != null ? rs.getTimestamp("fechaGeneracion").toLocalDateTime() : null)
			.fechaVencimiento(rs.getTimestamp("fechaVencimiento") != null ? rs.getTimestamp("fechaVencimiento").toLocalDateTime() : null)
			.diasAtraso(rs.getObject("diasAtraso") != null ? rs.getInt("diasAtraso") : null)
			.importeCargo(rs.getBigDecimal("importeCargo") != null ? rs.getBigDecimal("importeCargo") : BigDecimal.ZERO)
			.importeAbono(rs.getBigDecimal("importeAbono") != null ? rs.getBigDecimal("importeAbono") : BigDecimal.ZERO)
			.importePendiente(rs.getBigDecimal("importePendiente") != null ? rs.getBigDecimal("importePendiente") : BigDecimal.ZERO)
			.interesMoratorio(rs.getBigDecimal("interesMoratorio") != null ? rs.getBigDecimal("interesMoratorio") : BigDecimal.ZERO)
			.totalAPagar(rs.getBigDecimal("totalAPagar") != null ? rs.getBigDecimal("totalAPagar") : BigDecimal.ZERO)
			.concepto(rs.getString("concepto"))
			.detalle(rs.getString("detalle"))
			.tipoMovimiento(rs.getString("tipoMovimiento"))
			.estatusNombre(rs.getString("estatusNombre"))
			.desarrolloNombre(rs.getString("desarrolloNombre"))
			.desarrolloId(rs.getObject("desarrolloId") != null ? rs.getInt("desarrolloId") : null)
			.usuarioCaptura(rs.getString("usuarioCaptura"))
			.build();

	public List<EstadoCuentaAdeudoDto> spFacilObtenerEstadoCuentaAdeudo(EstadoCuentaAdeudoRequest request, Integer idDesarrolloUsuario) {
		Map<String, Object> params = new HashMap<>();
		params.put("Membresia", request.membresia());
		params.put("FechaCorte", request.fechaCorte());
		params.put("IdUsuarioDesarrollo", idDesarrolloUsuario);

		return executor.queryList("spFacilObtenerEstadoCuentaAdeudo", params, estadoCuentaAdeudoRowMapper);
	}
}
