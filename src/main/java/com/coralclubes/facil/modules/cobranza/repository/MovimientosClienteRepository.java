package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.facil.modules.cobranza.dto.request.EstadoCuentaAdeudoRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.HistoricoMovimientosRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.EstadoCuentaAdeudoDto;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoHistoricoDto;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoHistoricoPdfDto;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.utils.json.JsonUtils;
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

		return executor.queryList("spCobranzaObtenerHistoricoMovimientos", params, movimientoHistoricoRowMapper);
	}

	public List<MovimientoHistoricoPdfDto> spCobranzaObtenerHistoricoMovimientosPdf(String membresia, LocalDateTime fechaCorte) {
		Map<String, Object> params = new HashMap<>();
		params.put("Membresia", membresia);
		params.put("FechaCorte", fechaCorte);

		return executor.queryList("spCobranzaObtenerHistoricoMovimientosPdf", params, movimientoHistoricoPdfRowMapper);
	}
}
