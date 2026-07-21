package com.coralclubes.facil.modules.clientes.repository;

import com.coralclubes.facil.modules.clientes.dto.response.BeneficiosReferidosResponse;
import com.coralclubes.facil.modules.clientes.dto.response.DetalleConsumoReferidoResponse;
import com.coralclubes.facil.modules.clientes.dto.response.MembresiaReferidoDto;
import com.coralclubes.facil.modules.clientes.dto.response.ResumenReferidosResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReferidosRepository {

    private final StoredProcedureExecutor executor;

    private final RowMapper<BeneficiosReferidosResponse> beneficiosMapper = (rs, rowNum) ->
            BeneficiosReferidosResponse.builder()
                    .membresiaReferidor(rs.getString("MembresiaReferidor"))
                    .nombreSocioReferidor(rs.getString("NombreSocioReferidor"))
                    .consecutivoReferido(rs.getObject("ConsecutivoReferido", Integer.class))
                    .membresiaReferido(rs.getString("MembresiaReferido"))
                    .nombreSocioReferido(rs.getString("NombreSocioReferido"))
                    .totalReferidos(rs.getObject("TotalReferidos", Integer.class))
                    .estatusReferidos(rs.getString("EstatusReferidos"))
                    .porcentajeDescuentoAplicar(rs.getBigDecimal("PorcentajeDescuentoAplicar"))
                    .montoDescuentoDinero(rs.getBigDecimal("MontoDescuentoDinero"))
                    .montoConsumido(rs.getBigDecimal("MontoConsumido"))
                    .puntosPorDescontar(rs.getObject("PuntosPorDescontar", Integer.class))
                    .puntosDescontados(rs.getObject("PuntosDescontados", Integer.class))
                    .usuarioRegistroBeneficio(rs.getString("UsuarioRegistroBeneficio"))
                    .fechaRegistroBeneficio(rs.getTimestamp("FechaRegistroBeneficio") != null ? rs.getTimestamp("FechaRegistroBeneficio").toLocalDateTime() : null)
                    .usuarioRegistroReferido(rs.getString("UsuarioRegistroReferido"))
                    .fechaRegistroReferido(rs.getTimestamp("FechaRegistroReferido") != null ? rs.getTimestamp("FechaRegistroReferido").toLocalDateTime() : null)
                    .build();

    private final RowMapper<DetalleConsumoReferidoResponse> detalleConsumoMapper = (rs, rowNum) ->
            DetalleConsumoReferidoResponse.builder()
                    .membresiaReferidor(rs.getString("MembresiaReferidor"))
                    .membresiaReferido(rs.getString("MembresiaReferido"))
                    .consecutivoReferido(rs.getObject("ConsecutivoReferido", Integer.class))
                    .numConsumo(rs.getObject("NumConsumo", Integer.class))
                    .fechaRegistroConsumo(rs.getTimestamp("FechaRegistroConsumo") != null ? rs.getTimestamp("FechaRegistroConsumo").toLocalDateTime() : null)
                    .fechaGeneracionMovimiento(rs.getTimestamp("FechaGeneracionMovimiento") != null ? rs.getTimestamp("FechaGeneracionMovimiento").toLocalDateTime() : null)
                    .descripcion(rs.getString("Descripcion"))
                    .cargo(rs.getBigDecimal("Cargo"))
                    .abono(rs.getBigDecimal("Abono"))
                    .usuarioRegistro(rs.getString("UsuarioRegistro"))
                    .idMovimiento(rs.getObject("IdMovimiento", Integer.class))
                    .build();

    private final RowMapper<ResumenReferidosResponse> resumenMapper = (rs, rowNum) ->
            ResumenReferidosResponse.builder()
                    .membresiaReferidor(rs.getString("MembresiaReferidor"))
                    .totalReferidos(rs.getObject("TotalReferidos", Integer.class))
                    .montoAsignadoGlobal(rs.getBigDecimal("MontoAsignadoGlobal"))
                    .montoConsumidoGlobal(rs.getBigDecimal("MontoConsumidoGlobal"))
                    .montoDisponibleGlobal(rs.getBigDecimal("MontoDisponibleGlobal"))
                    .build();

    private final RowMapper<MembresiaReferidoDto> referidoRowMapper = (rs, rowNum) ->
            MembresiaReferidoDto.builder()
                    .consecutivoReferido(rs.getObject("consecutivo_referido", Integer.class))
                    .apellidoPaterno(rs.getString("apellido_paterno"))
                    .apellidoMaterno(rs.getString("apellido_materno"))
                    .nombre(rs.getString("nombre"))
                    .segundoNombre(rs.getString("segundo_nombre"))
                    .nombreCompleto(rs.getString("nombre_completo"))
                    .genero(rs.getString("genero"))
                    .parentesco(rs.getString("parentesco"))
                    .emailPrincipal(rs.getString("email_principal"))
                    .emailAlterno(rs.getString("email_alterno"))
                    .telefono(rs.getString("telefono"))
                    .tipoCliente(rs.getString("tipo_cliente"))
                    .membresia(rs.getString("membresia"))
                    .build();

    public List<BeneficiosReferidosResponse> spMembresiaObtenerBeneficiosReferidos(String membresia) {
        Map<String, Object> params = Map.of("Membresia", membresia);
        return executor.queryList("spMembresiaObtenerBeneficiosReferidos", params, beneficiosMapper);
    }

    public List<DetalleConsumoReferidoResponse> spMembresiaObtenerDetalleConsumoReferido(String membresiaReferidor, Integer consecutivoReferido) {
        Map<String, Object> params = new HashMap<>();
        params.put("MembresiaReferidor", membresiaReferidor);
        params.put("ConsecutivoReferido", consecutivoReferido);
        return executor.queryList("spMembresiaObtenerDetalleConsumoReferido", params, detalleConsumoMapper);
    }

    public Optional<ResumenReferidosResponse> spMembresiaObtenerResumenReferidos(String membresia) {
        Map<String, Object> params = Map.of("Membresia", membresia);
        return executor.querySingle("spMembresiaObtenerResumenReferidos", params, resumenMapper);
    }

    public List<MembresiaReferidoDto> spMembresiaObtenerReferidos(String membresia) {
        Map<String, Object> params = Map.of("Membresia", membresia);
        return executor.queryList("spMembresiaObtenerReferidos", params, referidoRowMapper);
    }
}
