package com.coralclubes.facil.modules.clientes.repository;

import com.coralclubes.facil.modules.clientes.dto.response.BeneficiarioDto;
import com.coralclubes.facil.modules.clientes.dto.response.MembresiaDatosDto;
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

    private final RowMapper<BeneficiarioDto> beneficiarioRowMapper = (rs, rowNum) -> BeneficiarioDto.builder()
            .numeroBeneficiario(rs.getObject("numero_beneficiario") != null ? rs.getInt("numero_beneficiario") : null)
            .nombreCompleto(rs.getString("nombre_completo"))
            .fechaNacimiento(rs.getTimestamp("fecha_nacimiento") != null ? rs.getTimestamp("fecha_nacimiento").toLocalDateTime() : null)
            .fechaRegistro(rs.getTimestamp("fecha_registro") != null ? rs.getTimestamp("fecha_registro").toLocalDateTime() : null)
            .correoElectronico(rs.getString("correo_electronico"))
            .genero(rs.getString("genero"))
            .parentesco(rs.getString("parentesco"))
            .tipoCliente(rs.getString("tipo_cliente"))
            .estatusCliente(rs.getString("estatus_cliente"))
            .estadoCivil(rs.getString("estado_civil"))
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

    public List<BeneficiarioDto> spClienteObtenerBeneficiariosMembresia(String membresia) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);

        return spExecutor.queryList(
                "spClienteObtenerBeneficiariosMembresia",
                params,
                beneficiarioRowMapper
        );
    }
}
