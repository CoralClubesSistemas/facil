package com.coralclubes.facil.modules.clientes.repository;

import com.coralclubes.facil.modules.clientes.dto.response.BeneficiarioDto;
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
            .anioVigencia(rs.getObject("anio_vigencia") != null ? rs.getInt("anio_vigencia") : null)
            .mesVigencia(rs.getObject("mes_vigencia") != null ? rs.getInt("mes_vigencia") : null)
            .ultimoMovimiento(rs.getString("ultimo_movimiento"))
            .tieneAccesoPreferencial(rs.getObject("tiene_acceso_preferencial") != null && rs.getInt("tiene_acceso_preferencial") == 1)
            .aplicaAccesoPreferencial(rs.getObject("aplica_acceso_preferencial", Boolean.class))
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

    public void spMembresiaBloqueaBeneficiario(
            String membresia,
            Integer numBeneficiario,
            String usuario,
            String motivo,
            java.time.LocalDate fechaInicio,
            java.time.LocalDate fechaFin
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("membresia", membresia);
        params.put("numbeneficiario", numBeneficiario);
        params.put("usuario", usuario);
        params.put("motivo", motivo);
        params.put("fecha_inicio", fechaInicio);
        params.put("fecha_fin", fechaFin != null ? fechaFin : LocalDateTime.of(2999, 12, 31, 23, 59, 59));
        
        spExecutor.execute("spMembresiaBloqueaBeneficiario", params);
    }

    public void spMembresiaDesbloqueaBeneficiario(String membresia, Integer numBeneficiario, String usuario) {
        Map<String, Object> params = Map.of(
                "membresia", membresia,
                "numbeneficiario", numBeneficiario,
                "usuario", usuario
        );
        spExecutor.execute("spMembresiaDesbloqueaBeneficiario", params);
    }
}
