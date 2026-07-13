package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.facil.modules.cobranza.dto.request.EstadoCuentaAdeudoRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.HistoricoMovimientosRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.EstadoCuentaAdeudoDto;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoHistoricoDto;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoHistoricoPdfDto;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSetMetaData;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class MovimientosClienteRepository {

	private final StoredProcedureExecutor executor;
	private final JdbcTemplate jdbcTemplate;

	private final RowMapper<Map<String, Object>> dynamicRowMapper = (rs, rowNum) -> {
		Map<String, Object> map = new LinkedHashMap<>();
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		for (int i = 1; i <= columnCount; i++) {
			String columnLabel = metaData.getColumnLabel(i);
			map.put(columnLabel, rs.getObject(i));
		}
		return map;
	};

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

	private final RowMapper<MovimientoHistoricoDto> movimientoHistoricoRowMapper = (rs, rowNum) -> MovimientoHistoricoDto.builder()
			.id(rs.getObject("id") != null ? rs.getLong("id") : null)
			.numeroPlan(rs.getString("numero_plan"))
			.idTipoMovimiento(rs.getString("id_tipo_movimiento"))
			.tipoMovimiento(rs.getString("tipo_movimiento"))
			.fechaGeneracion(rs.getTimestamp("fecha_generacion") != null ? rs.getTimestamp("fecha_generacion").toLocalDateTime() : null)
			.fechaVencimiento(rs.getTimestamp("fecha_vencimiento") != null ? rs.getTimestamp("fecha_vencimiento").toLocalDateTime() : null)
			.importeCargo(rs.getBigDecimal("importe_cargo") != null ? rs.getBigDecimal("importe_cargo") : BigDecimal.ZERO)
			.importeAbono(rs.getBigDecimal("importe_abono") != null ? rs.getBigDecimal("importe_abono") : BigDecimal.ZERO)
			.importePendiente(rs.getBigDecimal("importe_pendiente") != null ? rs.getBigDecimal("importe_pendiente") : BigDecimal.ZERO)
			.usuario(rs.getString("usuario"))
			.estatus(rs.getString("estatus"))
			.idDesarrolloConsumo(rs.getObject("id_desarrollo_consumo") != null ? rs.getInt("id_desarrollo_consumo") : null)
			.desarrolloConsumo(rs.getString("desarrollo_consumo"))
			.conceptoDescripcion(rs.getString("concepto_descripcion"))
			.descripcionMovimiento(rs.getString("descripcion_movimiento"))
			.numeroRecibo(rs.getObject("numero_recibo") != null ? rs.getInt("numero_recibo") : null)
			.idSerieRecibo(rs.getObject("id_serie_recibo") != null ? rs.getInt("id_serie_recibo") : null)
			.folioRecibo(rs.getString("folio_recibo"))
			.fechaPagoRecibo(rs.getTimestamp("fecha_pago_recibo") != null ? rs.getTimestamp("fecha_pago_recibo") : null)
			.cantidadMovimientosFamilia(rs.getObject("cantidad_movimientos_familia") != null ? rs.getInt("cantidad_movimientos_familia") : null)
			.build();

	private final RowMapper<MovimientoHistoricoPdfDto> movimientoHistoricoPdfRowMapper = (rs, rowNum) -> MovimientoHistoricoPdfDto.builder()
			.id(rs.getObject("id") != null ? rs.getLong("id") : null)
			.familiaId(rs.getObject("familia_id") != null ? rs.getInt("familia_id") : null)
			.padreId(rs.getObject("padre_id") != null ? rs.getInt("padre_id") : null)
			.tipoMovimiento(rs.getString("tipo_movimiento"))
			.fechaGeneracion(rs.getTimestamp("fecha_generacion") != null ? rs.getTimestamp("fecha_generacion").toLocalDateTime() : null)
			.fechaVencimiento(rs.getTimestamp("fecha_vencimiento") != null ? rs.getTimestamp("fecha_vencimiento").toLocalDateTime() : null)
			.importeCargo(rs.getBigDecimal("importe_cargo") != null ? rs.getBigDecimal("importe_cargo") : BigDecimal.ZERO)
			.importeAbono(rs.getBigDecimal("importe_abono") != null ? rs.getBigDecimal("importe_abono") : BigDecimal.ZERO)
			.importePendiente(rs.getBigDecimal("importe_pendiente") != null ? rs.getBigDecimal("importe_pendiente") : BigDecimal.ZERO)
			.interesMoratorio(rs.getBigDecimal("interes_moratorio") != null ? rs.getBigDecimal("interes_moratorio") : BigDecimal.ZERO)
			.conceptoDescripcion(rs.getString("concepto_descripcion"))
			.descripcionMovimiento(rs.getString("descripcion_movimiento"))
			.folioRecibo(rs.getString("folio_recibo"))
			.fechaPagoRecibo(rs.getTimestamp("fecha_pago_recibo") != null ? rs.getTimestamp("fecha_pago_recibo").toLocalDateTime() : null)
			.build();

	public List<EstadoCuentaAdeudoDto> spFacilObtenerEstadoCuentaAdeudo(EstadoCuentaAdeudoRequest request, Integer idDesarrolloUsuario) {
		Map<String, Object> params = new HashMap<>();
		params.put("Membresia", request.membresia());
		params.put("FechaCorte", request.fechaCorte());
		params.put("IdUsuarioDesarrollo", idDesarrolloUsuario);

		return executor.queryList("spFacilObtenerEstadoCuentaAdeudo", params, estadoCuentaAdeudoRowMapper);
	}

	public List<MovimientoHistoricoDto> spCobranzaObtenerHistoricoMovimientos(HistoricoMovimientosRequest request) {
		Map<String, Object> params = new HashMap<>();
		params.put("Membresia", request.membresia());
		
		String tipoMovimientosJson = null;
		if (request.tipoMovimientos() != null && !request.tipoMovimientos().isEmpty()) {
			tipoMovimientosJson = JsonUtils.toJson(request.tipoMovimientos());
		}
		
		params.put("TipoMovimientos", tipoMovimientosJson);
		params.put("EstatusMovimientos", request.estatusMovimientos());
		params.put("DesarrolloConsumo", request.desarrolloConsumo());
		params.put("IdPadre", request.idPadre());
		params.put("NumeroRecibo", request.numeroRecibo());
		params.put("SerieRecibo", request.serieRecibo());
		params.put("FechaPago", request.fechaPago());

		return executor.queryList("spCobranzaObtenerHistoricoMovimientos", params, movimientoHistoricoRowMapper);
	}

	public List<MovimientoHistoricoPdfDto> spCobranzaObtenerHistoricoMovimientosPdf(String membresia, LocalDateTime fechaCorte) {
		Map<String, Object> params = new HashMap<>();
		params.put("Membresia", membresia);
		params.put("FechaCorte", fechaCorte);

		return executor.queryList("spCobranzaObtenerHistoricoMovimientosPdf", params, movimientoHistoricoPdfRowMapper);
	}

	public List<Map<String, Object>> spCobranzaObtenerHistoricoMovimientosExcel(String membresia) {
		Map<String, Object> params = new HashMap<>();
		params.put("Membresia", membresia);

		return executor.queryList("spCobranzaObtenerHistoricoMovimientosExcel", params, dynamicRowMapper);
	}

	public List<String> spCobranzaObtenerColumnasExcel(String membresia) {
		String callString = "{call spCobranzaObtenerHistoricoMovimientosExcel(?)}";
		return jdbcTemplate.execute(
				(java.sql.Connection con) -> {
					try (java.sql.CallableStatement cs = con.prepareCall(callString)) {
						cs.setString(1, membresia);
						boolean hasResult = cs.execute();
						if (hasResult) {
							try (java.sql.ResultSet rs = cs.getResultSet()) {
								ResultSetMetaData metaData = rs.getMetaData();
								List<String> columnas = new java.util.ArrayList<>();
								for (int i = 1; i <= metaData.getColumnCount(); i++) {
									columnas.add(metaData.getColumnLabel(i));
								}
								return columnas;
							}
						}
						return java.util.Collections.emptyList();
					}
				}
		);
	}
}
