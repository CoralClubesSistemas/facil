package com.coralclubes.facil.modules.clientes.repository;

import com.coralclubes.facil.modules.clientes.dto.response.*;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MembresiaRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<MembresiaCancelacionDto> cancelacionRowMapper = (rs, rowNum) -> MembresiaCancelacionDto.builder()
            .membresia(rs.getString("membresia"))
            .consecutivo(rs.getObject("consecutivo") != null ? rs.getInt("consecutivo") : null)
            .estatusMembresia(rs.getString("estatusMembresia"))
            .motivoBaja(rs.getString("motivoBaja"))
            .razonConvenio(rs.getString("razonConvenio"))
            .usuarioRegistro(rs.getString("usuarioRegistro"))
            .fechaRegistro(rs.getTimestamp("fechaRegistro") != null ? rs.getTimestamp("fechaRegistro").toLocalDateTime() : null)
            .build();

    private final RowMapper<MembresiaAfiliacionDto> afiliacionRowMapper = (rs, rowNum) -> MembresiaAfiliacionDto.builder()
            .membresia(rs.getString("membresia"))
            .diasDeCorte(rs.getString("diasDeCorte"))
            .plantillaCargoAutomatico(rs.getString("plantillaCargoAutomatico"))
            .numeroTarjetaEnmascarada(rs.getString("numeroTarjetaEnmascarada"))
            .vigenciaTarjeta(rs.getString("vigenciaTarjeta"))
            .banco(rs.getString("banco"))
            .prioridad(rs.getString("prioridad"))
            .fechaRegistro(rs.getTimestamp("fechaRegistro") != null ? rs.getTimestamp("fechaRegistro").toLocalDateTime() : null)
            .usuarioRegistro(rs.getString("usuarioRegistro"))
            .build();

    private final RowMapper<MembresiaVigenciaDto> vigenciaRowMapper = (rs, rowNum) -> MembresiaVigenciaDto.builder()
            .inicioVigencia(rs.getTimestamp("InicioVigencia") != null ? rs.getTimestamp("InicioVigencia").toLocalDateTime() : null)
            .vigencia(rs.getObject("Vigencia") != null ? rs.getInt("Vigencia") : null)
            .unidadVigencia(rs.getString("UnidadVigencia"))
            .finalVigencia(rs.getTimestamp("FinalVigencia") != null ? rs.getTimestamp("FinalVigencia").toLocalDateTime() : null)
            .fechaFinalAmpliacion(rs.getString("FechaFinalAmpliacion"))
            .idConceptoAmpliacion(rs.getObject("IdConceptoAmpliacion") != null ? rs.getInt("IdConceptoAmpliacion") : null)
            .conceptoAmpliacion(rs.getString("ConceptoAmpliacion"))
            .build();

    private final RowMapper<MembresiaAccesosFinSemanaDto> accesosFinSemanaRowMapper = (rs, rowNum) -> MembresiaAccesosFinSemanaDto.builder()
            .numeroTrimestre(rs.getObject("numeroTrimestre") != null ? rs.getInt("numeroTrimestre") : null)
            .accesosPermitidos(rs.getObject("accesosPermitidos") != null ? rs.getInt("accesosPermitidos") : null)
            .accesosUtilizados(rs.getObject("accesosUtilizados") != null ? rs.getInt("accesosUtilizados") : null)
            .ultimaFechaIngreso(rs.getTimestamp("ultimaFechaIngreso") != null ? rs.getTimestamp("ultimaFechaIngreso").toLocalDateTime() : null)
            .build();

    private final RowMapper<MembresiaDetallesPlanVentaDto> detallesPlanVentaRowMapper = (rs, rowNum) -> MembresiaDetallesPlanVentaDto.builder()
            .numeroPlan(rs.getObject("numeroPlan") != null ? rs.getInt("numeroPlan") : null)
            .idEstatusPlan(rs.getObject("idEstatusPlan") != null ? rs.getInt("idEstatusPlan") : null)
            .estatusPlan(rs.getString("estatusPlan"))
            .precioPlan(rs.getBigDecimal("precioPlan"))
            .descuento(rs.getBigDecimal("descuento"))
            .porcentajeDescuento(rs.getBigDecimal("porcentajeDescuento"))
            .montoNeto(rs.getBigDecimal("montoNeto"))
            .enganche(rs.getBigDecimal("enganche"))
            .porcentajeEnganche(rs.getBigDecimal("porcentajeEnganche"))
            .intereses(rs.getBigDecimal("intereses"))
            .porcentajeIntereses(rs.getBigDecimal("porcentajeIntereses"))
            .saldo(rs.getBigDecimal("saldo"))
            .numeroMensualidades(rs.getObject("numeroMensualidades") != null ? rs.getInt("numeroMensualidades") : null)
            .importeMensualidades(rs.getBigDecimal("importeMensualidades"))
            .mensualidadesGeneradas(rs.getObject("mensualidadesGeneradas") != null ? rs.getInt("mensualidadesGeneradas") : null)
            .mensualidadesPendientes(rs.getObject("mensualidadesPendientes") != null ? rs.getInt("mensualidadesPendientes") : null)
            .importeUltimaMensualidad(rs.getBigDecimal("importeUltimaMensualidad"))
            .inicioMensualidades(rs.getTimestamp("inicioMensualidades") != null ? rs.getTimestamp("inicioMensualidades").toLocalDateTime() : null)
            .fechaVenta(rs.getTimestamp("fechaVenta") != null ? rs.getTimestamp("fechaVenta").toLocalDateTime() : null)
            .periodicidadMantenimientoId(rs.getObject("idPeriodicidadMantenimiento") != null ? rs.getInt("idPeriodicidadMantenimiento") : null)
            .periodicidadMantenimiento(rs.getString("periodicidadMantenimiento"))
            .build();


    public Optional<MembresiaCancelacionDto> spMembresiaObtenerDatosCancelacion(String membresia) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        return spExecutor.querySingle("spMembresiaObtenerDatosCancelacion", params, cancelacionRowMapper);
    }

    public Optional<MembresiaAfiliacionDto> spMembresiaAfiliacionCargoAutomatico(String membresia) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        return spExecutor.querySingle("spMembresiaAfiliacionCargoAutomatico", params, afiliacionRowMapper);
    }

    public Optional<MembresiaVigenciaDto> spMembresiaObtenerVigencia(String membresia) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        return spExecutor.querySingle("spMembresiaObtenerVigencia", params, vigenciaRowMapper);
    }

    public Optional<MembresiaAccesosFinSemanaDto> spMembresiaObtenerAccesosFinDeSemana(String membresia) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        return spExecutor.querySingle("spMembresiaObtenerAccesosFinDeSemana", params, accesosFinSemanaRowMapper);
    }

    public Optional<MembresiaDetallesPlanVentaDto> spMembresiaObtenerDetallesPlanVenta(String membresia, Integer plan) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("Plan", plan);
        return spExecutor.querySingle("spMembresiaObtenerDetallesPlanVenta", params, detallesPlanVentaRowMapper);
    }


    private final RowMapper<MembresiaDetalleProcesableDto> detalleProcesableRowMapper = (rs, rowNum) -> MembresiaDetalleProcesableDto.builder()
            .numeroPlan(rs.getObject("numeroPlan") != null ? rs.getInt("numeroPlan") : null)
            .montoProcesable(rs.getBigDecimal("montoProcesable"))
            .fechaProcesable(rs.getTimestamp("fechaProcesable") != null ? rs.getTimestamp("fechaProcesable").toLocalDateTime() : null)
            .montoBonificado(rs.getBigDecimal("montoBonificado"))
            .montoPagado(rs.getBigDecimal("montoPagado"))
            .estatusProcesable(rs.getString("estatusProcesable"))
            .build();

    public Optional<MembresiaDetalleProcesableDto> spMembresiaObtenerDetalleProcesable(String membresia) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        return spExecutor.querySingle("spMembresiaObtenerDetalleProcesable", params, detalleProcesableRowMapper);
    }

    private final RowMapper<MembresiaTemporalDto> temporalRowMapper = (rs, rowNum) -> MembresiaTemporalDto.builder()
            .desarrollo(rs.getString("desarrollo"))
            .estatusMembresia(rs.getString("estatus_membresia"))
            .membresia(rs.getString("membresia"))
            .socioTemporal(rs.getString("socio_temporal"))
            .fechaVenta(rs.getTimestamp("fecha_venta") != null ? rs.getTimestamp("fecha_venta").toLocalDateTime() : null)
            .build();

    public List<MembresiaTemporalDto> spMembresiaObtenerMembresiasTemporales(String numeroMembresia) {
        Map<String, Object> params = new HashMap<>();
        params.put("NumeroMembresia", numeroMembresia);
        return spExecutor.queryList("spMembresiaObtenerMembresiasTemporales", params, temporalRowMapper);
    }

    private final RowMapper<MembresiaAccesoDto> accesoRowMapper = (rs, rowNum) ->
            MembresiaAccesoDto.builder()
                    .totalRegistros(rs.getObject("total_registros", Integer.class))
                    .membresia(rs.getString("membresia"))
                    .numeroBeneficiario(rs.getObject("numero_beneficiario", Integer.class))
                    .nombreBeneficiario(rs.getString("nombre_beneficiario"))
                    .idDesarrolloAcceso(rs.getObject("id_desarrollo_acceso", Integer.class))
                    .desarrolloAcceso(rs.getString("desarrollo_acceso"))
                    .fechaAcceso(rs.getTimestamp("fecha_acceso") != null ? rs.getTimestamp("fecha_acceso").toLocalDateTime() : null)
                    .diaAcceso(rs.getString("dia_acceso"))
                    .trimestre(rs.getString("trimestre"))
                    .numeroAutorizacion(rs.getObject("numero_autorizacion", Integer.class))
                    .claveConceptoAutorizacion(rs.getObject("clave_concepto_autorizacion", Integer.class))
                    .conceptoAutorizacion(rs.getString("concepto_autorizacion"))
                    .fechaAutorizacion(rs.getString("fecha_autorizacion"))
                    .usuarioAutoriza(rs.getString("usuario_autoriza"))
                    .usuarioAccesa(rs.getString("usuario_accesa"))
                    .membresiaAsociada(rs.getString("membresia_asociada"))
                    .nombreInvitado(rs.getString("nombre_invitado"))
                    .esFestivo(rs.getObject("es_festivo", Integer.class) != null && rs.getInt("es_festivo") == 1)
                    .tipoAcceso(rs.getString("tipo_acceso"))
                    .tipoPromocion(rs.getString("tipo_promocion"))
                    .build();

    public List<MembresiaAccesoDto> spMembresiaObtenerAccesos(
            String membresia,
            String desarrollo,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Boolean soloFS,
            Integer numeroPagina,
            Integer registrosPorPagina
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("Desarrollo", desarrollo);
        params.put("FechaDesde", fechaDesde);
        params.put("FechaHasta", fechaHasta);
        params.put("SoloFS", soloFS);
        params.put("NumeroPagina", numeroPagina);
        params.put("RegistrosPorPagina", registrosPorPagina);

        return spExecutor.queryList("spMembresiaObtenerAccesos", params, accesoRowMapper);
    }

    private final RowMapper<MembresiaAccesoEntradaSalidaDto> entradaSalidaRowMapper = (rs, rowNum) ->
            MembresiaAccesoEntradaSalidaDto.builder()
                    .membresia(rs.getString("membresia"))
                    .numeroBeneficiario(rs.getObject("numero_beneficiario", Integer.class))
                    .idDesarrolloAcceso(rs.getObject("id_desarrollo_acceso", Integer.class))
                    .fechaAcceso(rs.getTimestamp("fecha_acceso") != null ? rs.getTimestamp("fecha_acceso").toLocalDateTime() : null)
                    .tipoAcceso(rs.getString("tipo_acceso"))
                    .usuarioRegistro(rs.getString("usuario_registro"))
                    .fechaRegistro(rs.getTimestamp("fecha_registro") != null ? rs.getTimestamp("fecha_registro").toLocalDateTime() : null)
                    .build();

    public List<MembresiaAccesoEntradaSalidaDto> spMembresiaObtenerAccesosEntradasSalidas(
            String membresia,
            Integer desarrollo,
            LocalDate fechaAccesoDesde,
            Integer beneficiario
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("Desarrollo", desarrollo);
        params.put("FechaAccesoDesde", fechaAccesoDesde);
        params.put("Beneficiario", beneficiario);

        return spExecutor.queryList("spMembresiaObtenerAccesosEntradasSalidas", params, entradaSalidaRowMapper);
    }
}
