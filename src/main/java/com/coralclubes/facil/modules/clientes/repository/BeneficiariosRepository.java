package com.coralclubes.facil.modules.clientes.repository;

import com.coralclubes.facil.modules.clientes.dto.response.BeneficiarioDto;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class BeneficiariosRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<BeneficiarioDto> beneficiarioRowMapper = (rs, rowNum) -> BeneficiarioDto.builder()
            .numeroBeneficiario(rs.getObject("numero_beneficiario") != null ? rs.getInt("numero_beneficiario") : null)
            .nombreCompleto(rs.getString("nombre_completo"))
            .fechaNacimiento(rs.getTimestamp("fecha_nacimiento") != null ? rs.getTimestamp("fecha_nacimiento").toLocalDateTime() : null)
            .edad(rs.getObject("edad") != null ? rs.getInt("edad") : null)
            .fechaRegistro(rs.getTimestamp("fecha_registro") != null ? rs.getTimestamp("fecha_registro").toLocalDateTime() : null)
            .correoElectronico(rs.getString("correo_electronico"))
            .genero(rs.getString("genero"))
            .parentesco(rs.getString("parentesco"))
            .tipoCliente(rs.getString("tipo_cliente"))
            .estatusCliente(rs.getString("estatus_cliente"))
            .estadoCivil(rs.getString("estado_civil"))
            .numeroCredencial(rs.getString("numero_credencial"))
            .uuidCredencial(rs.getString("uuid_credencial"))
            .ultimoMovimiento(rs.getString("ultimo_movimiento"))
            .build();

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
