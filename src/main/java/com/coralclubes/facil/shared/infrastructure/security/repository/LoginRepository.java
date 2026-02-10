package com.coralclubes.facil.shared.infrastructure.security.repository;

import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.facil.modules.sistema.dto.projection.ModuloDtoResult;
import com.coralclubes.facil.shared.infrastructure.security.dto.projection.UserAutorizacionesResult;
import com.coralclubes.facil.shared.infrastructure.security.dto.projection.UserLoginResult;
import com.coralclubes.facil.shared.infrastructure.security.dto.request.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio para la gestión de autenticación y autorizaciones.
 * Utiliza StoredProcedureExecutor para una ejecución estandarizada y segura.
 */
@Repository
@RequiredArgsConstructor
public class LoginRepository {
    private final StoredProcedureExecutor spExecutor;

    // =========================================================================
    // MAPPERS (funciones lambda que reciben un ResultSet y lo mapean a un objeto)
    // =========================================================================

    private final RowMapper<UserLoginResult> userLoginMapper = (rs, rowNum) -> UserLoginResult.builder()
            .usuario(rs.getString("USUARIO"))
            .password(rs.getString("PASSWORD"))
            .idDesarrollo(rs.getObject("DESARROLLO") != null ? rs.getInt("DESARROLLO") : null)
            .desarrolloDescripcion(rs.getString("DESARROLLO_DESCRIPCION"))
            .email(rs.getString("CORREO"))
            .rolId(rs.getObject("ROL_ID") != null ? rs.getInt("ROL_ID") : null)
            .rolDescripcion(rs.getString("ROL_DESCRIPCION"))
            .build();

    private final RowMapper<ModuloDtoResult> moduloMapper = (rs, rowNum) -> ModuloDtoResult.builder()
            .id(rs.getInt("MODULO_ID"))
            .idPadre(rs.getInt("MODULO_PADRE_ID"))
            .clave(rs.getString("MODULO_CLAVE"))
            .nombre(rs.getString("MODULO_NOMBRE"))
            .ruta(rs.getString("MODULO_RUTA"))
            .icono(rs.getString("MODULO_ICONO"))
            .menuFacil(rs.getInt("MODULO_MENU"))
            .build();

    private final RowMapper<UserAutorizacionesResult> autorizacionMapper = (rs, rowNum) -> UserAutorizacionesResult.builder()
            .id(rs.getInt("AFP_ID"))
            .nombre(rs.getString("AFP_NOMBRE_AUTORIZACION"))
            .clave(rs.getString("AFP_CLAVE"))
            .build();

    private final RowMapper<LoginRequest> loginSimpleMapper = (rs, rowNum) -> new LoginRequest(
            rs.getString("USUARIO"),
            rs.getString("PASSWORD")
    );

    // Mapper para valores escalares únicos (como el conteo de autorizaciones)
    private final RowMapper<Integer> singleIntMapper = (rs, rowNum) -> rs.getInt(1);

    // =========================================================================
    // MÉTODOS DE ACCESO A DATOS
    // =========================================================================

    /**
     * Obtiene los detalles completos del usuario.
     */
    public Optional<UserLoginResult> spLoginUsuarios(String usuario) {
        // Usamos HashMap en lugar de Map.of para permitir nulos
        Map<String, Object> params = new HashMap<>();
        params.put("USUARIO", usuario);
        params.put("CORREO", null);

        return spExecutor.querySingle(
                "spLoginUsuarios",
                params,
                userLoginMapper
        );
    }

    /**
     * Obtiene los módulos asignados según el rol del usuario.
     */
    public List<ModuloDtoResult> spLoginModulosUsuarios(String usuario) {
        return spExecutor.queryList(
                "spLoginModulosUsuarios",
                Map.of("USUARIO", usuario),
                moduloMapper
        );
    }

    /**
     * Obtiene las autorizaciones específicas (permisos) del usuario.
     */
    public List<UserAutorizacionesResult> spLoginObtenerAutorizacionesUsuario(String usuario) {
        return spExecutor.queryList(
                "spLoginObtenerAutorizacionesUsuario",
                Map.of("USUARIO", usuario),
                autorizacionMapper
        );
    }

    /**
     * Retorna solo usuario y contraseña para validaciones rápidas.
     * logParams = false porque el resultado contiene contraseñas.
     */
    public LoginRequest spLoginSimple(String usuario) {
        return spExecutor.querySingle(
                "spLoginSimple",
                Map.of("USUARIO", usuario),
                loginSimpleMapper
        ).orElse(null);
    }

    /**
     * Valida si un usuario tiene una autorización específica.
     */
    public int spLoginValidarAutorizacionFueraDePolitica(String username, String autorizacion) {
        return spExecutor.querySingle(
                "spLoginValidarAutorizacionFueraDePolitica",
                Map.of(
                        "Usuario", username,
                        "AutorizacionClave", autorizacion
                ),
                singleIntMapper
        ).orElse(0);
    }
}