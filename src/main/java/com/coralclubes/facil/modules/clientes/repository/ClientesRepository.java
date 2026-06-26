package com.coralclubes.facil.modules.clientes.repository;

import com.coralclubes.facil.modules.clientes.dto.projection.ClienteLoginResult;
import com.coralclubes.facil.modules.clientes.dto.projection.ClienteValidacionMembresiaResult;
import com.coralclubes.facil.modules.clientes.dto.response.ValidacionCorreoDto;
import com.coralclubes.facil.modules.usuarios.dto.response.PasswordResetToken;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.logging.SqlLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ClientesRepository {

    private final StoredProcedureExecutor executor;
    private final JdbcTemplate jdbcTemplate;
    private final SqlLogger sqlLogger;

    private final RowMapper<ClienteValidacionMembresiaResult> validacionMembresiaMapper = (rs, rowNum) -> ClienteValidacionMembresiaResult.builder()
            .membresia(rs.getString("membresia"))
            .nombreCompleto(rs.getString("nombre_completo"))
            .desarrollo(rs.getObject("desarrollo") != null ? rs.getInt("desarrollo") : null)
            .descripcionDesarrollo(rs.getString("descripcion_desarrollo"))
            .estatus(rs.getObject("estatus") != null ? rs.getInt("estatus") : null)
            .descripcionEstatus(rs.getString("descripcion_estatus"))
            .correoPersonal(rs.getString("correo_personal"))
            .correoTrabajo(rs.getString("correo_trabajo"))
            .tipoMembresia(rs.getString("tipo_membresia"))
            .clasificacionMembresia(rs.getObject("clasificacion_membresia") != null ? rs.getInt("clasificacion_membresia") : null)
            .descripcionClasificacion(rs.getString("descripcion_clasificacion"))
            .claveClasificacion(rs.getString("clave_clasificacion"))
            .registroUserSystem(rs.getInt("registro_usersystem") == 1)
            .build();

    private final RowMapper<ClienteLoginResult> loginMapper = (rs, rowNum) -> ClienteLoginResult.builder()
            .idUsuario(rs.getString("id_usuario"))
            .membresia(rs.getString("membresia"))
            .correo(rs.getString("correo"))
            .tokenProveedor(rs.getString("token_proveedor"))
            .passwordHash(rs.getString("password_hash"))
            .build();

    // Mapper para convertir filas de la tabla password_reset_tokens a  Record/DTO
    private final RowMapper<PasswordResetToken> tokenMapper = (rs, rowNum) -> PasswordResetToken.builder()
            .username(rs.getString("username"))
            .email(rs.getString("email"))
            .token(rs.getString("token"))
            .expiryDate(rs.getTimestamp("expiry_date").toLocalDateTime())
            .build();

    private final RowMapper<ValidacionCorreoDto> validacionCorreoMapper = (rs, rowNum) -> new ValidacionCorreoDto(
            rs.getInt("correo_empleado") == 1,
            rs.getInt("correo_cliente_sin_registro") == 1,
            rs.getInt("correo_cliente_con_registro") == 1
    );

    public Optional<ClienteValidacionMembresiaResult> spClientesValidarMembresia(String membresia, String email) {
        Map<String, Object> params = Map.of("membresia", membresia, "correo", email);
        return executor.querySingle("spClientesValidarMembresia", params, validacionMembresiaMapper);
    }

    public void spClientesCrearAccesoWeb(String membresia, String correo, String passwordHash, String tokenProveedor) {
        Map<String, Object> params = new HashMap<>();
        params.put("membresia", membresia);
        params.put("correo", correo);
        params.put("password", passwordHash);
        params.put("token_proveedor", tokenProveedor);

        executor.execute("spClientesCrearAccesoWeb", params);
    }

    public Optional<ClienteLoginResult> spClientesLogin(String email) {
        Map<String, Object> params = Map.of("email", email);
        return executor.querySingle("spClientesLogin", params, loginMapper);
    }

    public Optional<String> buscarMembresiaPorFirebaseUid(String firebaseUid) {
        String sql = "SELECT USUARIO FROM USERSYSTEM WHERE token_auth = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, String.class, firebaseUid));
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<ValidacionCorreoDto> spUsuariosValidarCorreoExistente(String correo) {
        Map<String, Object> params = Map.of("correo", correo);
        return executor.querySingle("spUsuariosValidarCorreoExistente", params, validacionCorreoMapper);
    }
}
