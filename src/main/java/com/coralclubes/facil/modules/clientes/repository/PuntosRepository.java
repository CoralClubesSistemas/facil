package com.coralclubes.facil.modules.clientes.repository;

import com.coralclubes.facil.modules.clientes.dto.projection.DetalleCuentaPuntosProjection;
import com.coralclubes.facil.modules.clientes.dto.projection.PaquetePuntosPlanProjection;
import com.coralclubes.facil.modules.clientes.dto.request.ConsumoPuntosRequest;
import com.coralclubes.facil.modules.clientes.dto.request.DetalleCuentaPuntosRequest;
import com.coralclubes.facil.modules.clientes.dto.response.*;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PuntosRepository {
    private final StoredProcedureExecutor executor;

    private RowMapper<PuntosMembresia> puntosMembresiaRowMapper = (rs, rowNum) -> PuntosMembresia.builder()
            .membresia(rs.getString("Membresia"))
            .nombreSocio(rs.getString("NombreSocio"))
            .puntosLiberadosRegulares(rs.getInt("PuntosLiberadosRegulares"))
            .puntosLiberadosPromocion(rs.getInt("PuntosLiberadosPromocion"))
            .totalPuntosLiberados(rs.getInt("TotalPuntosLiberados"))
            .puntosConsumidos(rs.getInt("PuntosConsumidos"))
            .saldoPuntosNeto(rs.getInt("SaldoPuntosNeto"))
            .fechaEmisionReporte(rs.getTimestamp("FechaEmisionReporte"))
            .build();

    private final RowMapper<Integer> folioGeneradoMapper = (rs, rowNum) -> rs.getInt("FolioPuntosGenerado");

    private final RowMapper<ConsumoPuntosDto> consumoPuntosRowMapper = (rs, rowNum) -> ConsumoPuntosDto.builder()
            .numeroBeneficiario(rs.getObject("numero_beneficiario") != null ? rs.getInt("numero_beneficiario") : null)
            .nombreBeneficiario(rs.getString("nombre_beneficiario"))
            .consecutivoConsumo(rs.getObject("consecutivo_consumo") != null ? rs.getInt("consecutivo_consumo") : null)
            .idDesarrolloConsumo(rs.getObject("id_desarrollo_consumo") != null ? rs.getInt("id_desarrollo_consumo") : null)
            .desarrolloConsumo(rs.getString("desarrollo_consumo"))
            .tipoClienteAcceso(rs.getString("tipo_cliente_acceso"))
            .tipoAccesoDesarrollo(rs.getString("tipo_acceso_desarrollo"))
            .periodoUsoDesarrollo(rs.getString("periodo_uso_desarrollo"))
            .puntosHospedaje(rs.getObject("puntos_hospedaje") != null ? rs.getInt("puntos_hospedaje") : null)
            .puntosInstalaciones(rs.getObject("puntos_instalaciones") != null ? rs.getInt("puntos_instalaciones") : null)
            .puntosCampoGolf(rs.getObject("puntos_campo_golf") != null ? rs.getInt("puntos_campo_golf") : null)
            .numeroAutorizacion(rs.getString("numero_autorizacion"))
            .fechaConsumo(rs.getTimestamp("fecha_consumo") != null ? rs.getTimestamp("fecha_consumo").toLocalDateTime() : null)
            .descripcionMovimiento(rs.getString("descripcion_movimiento"))
            .build();

    private final RowMapper<PuntosLiberadosDto> puntosLiberadosRowMapper = (rs, rowNum) -> PuntosLiberadosDto.builder()
            .numeroPlan(rs.getObject("numero_plan") != null ? rs.getInt("numero_plan") : null)
            .idMovimiento(rs.getObject("id_movimiento") != null ? rs.getInt("id_movimiento") : null)
            .cantidadPuntos(rs.getObject("cantidad_puntos") != null ? rs.getInt("cantidad_puntos") : null)
            .fechaLiberacion(rs.getTimestamp("fecha_liberacion") != null ? rs.getTimestamp("fecha_liberacion").toLocalDateTime() : null)
            .conceptoLiberacion(rs.getString("concepto_liberacion"))
            .folioRecibo(rs.getString("folio_recibo"))
            .usuarioRegistro(rs.getString("usuario_registro"))
            .estatusPuntos(rs.getString("estatus_puntos"))
            .build();

    private final RowMapper<CuentaPuntosDto> cuentaPuntosRowMapper = (rs, rowNum) -> CuentaPuntosDto.builder()
            .cliId(rs.getObject("cli_id") != null ? rs.getInt("cli_id") : null)
            .membresia(rs.getString("membresia"))
            .nombreSocio(rs.getString("nombre_socio"))
            .numeroPlan(rs.getObject("numero_plan") != null ? rs.getInt("numero_plan") : null)
            .fechaInicio(rs.getTimestamp("fecha_inicio") != null ? rs.getTimestamp("fecha_inicio").toLocalDateTime() : null)
            .finalVigencia(rs.getTimestamp("final_vigencia") != null ? rs.getTimestamp("final_vigencia").toLocalDateTime() : null)
            .puntosMembresia(rs.getObject("puntos_membresia") != null ? rs.getInt("puntos_membresia") : null)
            .puntosEnganche(rs.getObject("puntos_enganche") != null ? rs.getInt("puntos_enganche") : null)
            .puntosMensualidades(rs.getObject("puntos_mensualidades") != null ? rs.getInt("puntos_mensualidades") : null)
            .descripcionMovimiento(rs.getString("descripcion_movimiento"))
            .puntosLiberados(rs.getObject("puntos_liberados") != null ? rs.getInt("puntos_liberados") : null)
            .puntosConsumidos(rs.getObject("puntos_consumidos") != null ? rs.getInt("puntos_consumidos") : null)
            .puntosHospedaje(rs.getObject("puntos_hospedaje") != null ? rs.getInt("puntos_hospedaje") : null)
            .puntosInstalaciones(rs.getObject("puntos_instalaciones") != null ? rs.getInt("puntos_instalaciones") : null)
            .puntosCampoGolf(rs.getObject("puntos_campogolf") != null ? rs.getInt("puntos_campogolf") : null)
            .saldoPuntos(rs.getObject("saldo_puntos") != null ? rs.getInt("saldo_puntos") : null)
            .estatusPuntos(rs.getString("estatus_puntos"))
            .fechaEmision(rs.getTimestamp("fecha_emision") != null ? rs.getTimestamp("fecha_emision").toLocalDateTime() : null)
            .build();

    private final RowMapper<PaquetePuntosPlanProjection> paquetePuntosPlanRowMapper = (rs, rowNum) -> PaquetePuntosPlanProjection.builder()
            .membresia(rs.getString("membresia"))
            .numeroPlan(rs.getObject("numeroPlan") != null ? rs.getInt("numeroPlan") : null)
            .fechaInicio(rs.getTimestamp("fechaInicio") != null ? rs.getTimestamp("fechaInicio").toLocalDateTime() : null)
            .finalVigencia(rs.getTimestamp("finalVigencia") != null ? rs.getTimestamp("finalVigencia").toLocalDateTime() : null)
            .puntosMembresia(rs.getObject("puntosMembresia") != null ? rs.getInt("puntosMembresia") : null)
            .puntosEnganche(rs.getObject("puntosEnganche") != null ? rs.getInt("puntosEnganche") : null)
            .puntosMensualidades(rs.getObject("puntosMensualidades") != null ? rs.getInt("puntosMensualidades") : null)
            .puntosLiberados(rs.getObject("puntosLiberados") != null ? rs.getInt("puntosLiberados") : null)
            .puntosConsumidos(rs.getObject("puntosConsumidos") != null ? rs.getInt("puntosConsumidos") : null)
            .estatusPlanPuntos(rs.getString("estatusPlanPuntos"))
            .build();

    private final RowMapper<DetalleCuentaPuntosProjection> detalleCuentaPuntosRowMapper = (rs, rowNum) -> DetalleCuentaPuntosProjection.builder()
            .numeroPlan(rs.getObject("numeroPlan") != null ? rs.getInt("numeroPlan") : null)
            .descripcionMovimiento(rs.getString("descripcionMovimiento"))
            .puntosLiberados(rs.getObject("puntosLiberados") != null ? rs.getInt("puntosLiberados") : null)
            .puntosConsumidos(rs.getObject("puntosConsumidos") != null ? rs.getInt("puntosConsumidos") : null)
            .puntosHospedaje(rs.getObject("puntosHospedaje") != null ? rs.getInt("puntosHospedaje") : null)
            .puntosInstalaciones(rs.getObject("puntosInstalaciones") != null ? rs.getInt("puntosInstalaciones") : null)
            .puntosCampogolf(rs.getObject("puntosCampogolf") != null ? rs.getInt("puntosCampogolf") : null)
            .saldoPuntos(rs.getObject("saldoPuntos") != null ? rs.getInt("saldoPuntos") : null)
            .estatusPuntos(rs.getString("estatusPuntos"))
            .numeroAutorizacion(rs.getString("numeroAutorizacion"))
            .usuario(rs.getString("usuario"))
            .fechaMovimiento(rs.getTimestamp("fechaMovimiento") != null ? rs.getTimestamp("fechaMovimiento").toLocalDateTime() : null)
            .build();

    public Integer spCliConsumirPuntos(ConsumoPuntosRequest request) {
        Map<String, Object> params = new HashMap<>();

        params.put("Membresia", request.membresia());
        params.put("DesarrolloId", request.desarrolloId());
        params.put("TotalPuntos", request.totalPuntos());
        params.put("PuntosHospedaje", request.puntosHospedaje() != null ? request.puntosHospedaje() : 0);
        params.put("PuntosInstalaciones", request.puntosInstalaciones() != null ? request.puntosInstalaciones() : 0);
        params.put("PuntosCampoGolf", request.puntosCampoGolf() != null ? request.puntosCampoGolf() : 0);
        params.put("ImportePuntos", 0);
        params.put("IdMovimiento", request.idMovimiento());
        params.put("Descripcion", request.descripcion());
        params.put("Usuario", request.usuario());
        params.put("NumBeneficiario", request.numBeneficiario() != null ? request.numBeneficiario() : 1);
        params.put("IdTipoCliente", request.idTipoCliente() != null ? request.idTipoCliente() : 0);
        params.put("IdTipoAcceso", request.idTipoAcceso() != null ? request.idTipoAcceso() : 0);
        params.put("IdPeriodoUso", request.idPeriodoUso() != null ? request.idPeriodoUso() : 0);

        return executor.querySingleLog("spCliConsumirPuntos", params, folioGeneradoMapper, request.usuario(), true, true)
                .orElseThrow(() -> new RuntimeException("No se pudo generar el folio de consumo de puntos en BD."));
    }

    public PuntosMembresia spSaldoPuntosDisponiblesMembresia(String membresia) {
        Map<String, Object> params = Map.of("Membresia", membresia);

        return executor.querySingle("spSaldoPuntosDisponiblesMembresia", params, puntosMembresiaRowMapper)
                .orElse(null);
    }

    public List<ConsumoPuntosDto> spClienteObtenerConsumoDePuntos(String membresia, LocalDateTime fechaCorte) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("FechaCorte", fechaCorte);

        return executor.queryList("spClienteObtenerConsumoDePuntos", params, consumoPuntosRowMapper);
    }

    public List<PuntosLiberadosDto> spClienteObtenerPuntosLiberados(String membresia, LocalDateTime fechaCorte) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("FechaCorte", fechaCorte);

        return executor.queryList("spClienteObtenerPuntosLiberados", params, puntosLiberadosRowMapper);
    }

    public List<CuentaPuntosDto> spClienteObtenerCuentaDePuntos(String membresia, LocalDateTime fechaCorte) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("FechaCorte", fechaCorte);

        return executor.queryList("spClienteObtenerCuentaDePuntos", params, cuentaPuntosRowMapper);
    }

    public List<PaquetePuntosPlanProjection> spMembresiaObtenerPaquetesPuntosPlan(String membresia) {
        Map<String, Object> params = Map.of("Membresia", membresia);
        return executor.queryList("spMembresiaObtenerPaquetesPuntosPlan", params, paquetePuntosPlanRowMapper);
    }

    public List<DetalleCuentaPuntosProjection> spMembresiaObtenerDetalleCuentaDePuntos(String membresia, DetalleCuentaPuntosRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("FechaInicio", request.fechaInicio());
        params.put("FechaFin", request.fechaFin());
        params.put("EstatusPuntos", request.estatusPuntos());
        params.put("NumeroPlan", request.numeroPlan());

        return executor.queryList("spMembresiaObtenerDetalleCuentaDePuntos", params, detalleCuentaPuntosRowMapper);
    }
}
