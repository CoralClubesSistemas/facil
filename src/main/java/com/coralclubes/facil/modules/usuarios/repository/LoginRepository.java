package com.coralclubes.facil.modules.usuarios.repository;

import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.facil.modules.usuarios.dto.projection.SimpleLoginResult;
import com.coralclubes.facil.modules.usuarios.dto.projection.UserAutorizacionesResult;
import com.coralclubes.facil.modules.usuarios.dto.projection.UserLoginResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio para la gestión de autenticación y autorizaciones.
 * Utilizado por InternalAuthService para validar credenciales
 * cuando el gateway delega el login.
 */
@Repository
@RequiredArgsConstructor
public class LoginRepository {
    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<UserLoginResult> userLoginMapper = (rs, rowNum) -> UserLoginResult.builder()
            .usuario(rs.getString("USUARIO"))
            .password(rs.getString("PASSWORD"))
            .idDesarrollo(rs.getObject("DESARROLLO") != null ? rs.getInt("DESARROLLO") : null)
            .desarrolloDescripcion(rs.getString("DESARROLLO_DESCRIPCION"))
            .email(rs.getString("CORREO"))
            .rolId(rs.getObject("ROL_ID") != null ? rs.getInt("ROL_ID") : null)
            .rolDescripcion(rs.getString("ROL_DESCRIPCION"))
            .nombreCompleto(rs.getString("NOMBRE_COMPLETO"))
            .build();



    private final RowMapper<UserAutorizacionesResult> autorizacionMapper = (rs, rowNum) -> UserAutorizacionesResult.builder()
            .id(rs.getInt("AFP_ID"))
            .nombre(rs.getString("AFP_NOMBRE_AUTORIZACION"))
            .clave(rs.getString("AFP_CLAVE"))
            .build();

    public Optional<UserLoginResult> spLoginUsuarios(String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("USUARIO", usuario);
        params.put("CORREO", null);

        return spExecutor.querySingle("spLoginUsuarios", params, userLoginMapper);
    }



    public List<UserAutorizacionesResult> spLoginObtenerAutorizacionesUsuario(String usuario) {
        return spExecutor.queryList("spLoginObtenerAutorizacionesUsuario", Map.of("USUARIO", usuario), autorizacionMapper);
    }

    private final RowMapper<SimpleLoginResult> simpleLoginMapper = (rs, rowNum) -> SimpleLoginResult.builder()
            .usuario(rs.getString("USUARIO"))
            .password(rs.getString("PASSWORD"))
            .build();

    private final RowMapper<Integer> singleIntMapper = (rs, rowNum) -> rs.getInt(1);

    public Optional<SimpleLoginResult> spLoginSimple(String usuario) {
        return spExecutor.querySingle("spLoginSimple", Map.of("USUARIO", usuario), simpleLoginMapper);
    }

    public int spLoginValidarAutorizacionFueraDePolitica(String username, String autorizacion) {
        return spExecutor.querySingle(
                "spLoginValidarAutorizacionFueraDePolitica",
                Map.of("Usuario", username, "AutorizacionClave", autorizacion),
                singleIntMapper
        ).orElse(0);
    }
}
