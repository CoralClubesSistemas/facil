package com.coralclubes.facil.modules.usuarios.repository;

import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import com.coralclubes.facil.modules.usuarios.dto.response.PasswordResetToken;
import com.coralclubes.logging.SqlLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio para la gestión de recuperación de contraseñas.
 */
@Repository
@RequiredArgsConstructor
public class PasswordResetRepository {

    private final JdbcTemplate jdbcTemplate;
    private final StoredProcedureExecutor spExecutor;
    private final SqlLogger sqlLogger;

    // Mapper para convertir filas de la tabla password_reset_tokens a  Record/DTO
    private final RowMapper<PasswordResetToken> tokenMapper = (rs, rowNum) -> PasswordResetToken.builder()
            .username(rs.getString("username"))
            .email(rs.getString("email"))
            .token(rs.getString("token"))
            .expiryDate(rs.getTimestamp("expiry_date").toLocalDateTime())
            .build();

    /**
     * Guarda un token de restablecimiento. Uso de SQL Nativo vía JdbcTemplate.
     */
    @Transactional
    public boolean savePasswordResetToken(PasswordResetToken tokenDto) {
        String sql = "INSERT INTO password_reset_tokens (username, email, token, expiry_date) VALUES (?, ?, ?, ?)";

        try {
            int rows = jdbcTemplate.update(sql,
                    tokenDto.username(),
                    tokenDto.email(),
                    tokenDto.token(),
                    Timestamp.valueOf(tokenDto.expiryDate()));
            return rows == 1;
        } catch (Exception e) {
            sqlLogger.logError("savePasswordResetToken", e);
            return false;
        }
    }

    /**
     * Busca un token activo.
     */
    public Optional<PasswordResetToken> findPasswordResetToken(String token) {
        String sql = "SELECT username, email, token, expiry_date FROM password_reset_tokens WHERE token = ?";

        try {
            List<PasswordResetToken> results = jdbcTemplate.query(sql, tokenMapper, token);
            return results.stream().findFirst();
        } catch (Exception e) {
            sqlLogger.logError("findPasswordResetToken", e);
            return Optional.empty();
        }
    }

    /**
     * Actualiza la contraseña
     */
    @Transactional
    public void spLoginCambiarPassword(String usuario, String newPassword) {
        spExecutor.execute(
                "spLoginCambiarPassword",
                Map.of("USUARIO", usuario, "PASSWORD", newPassword)
        );
    }

    /**
     * Elimina el token una vez utilizado o expirado.
     */
    @Transactional
    public boolean deletePasswordResetToken(String token) {
        String sql = "DELETE FROM password_reset_tokens WHERE token = ?";

        try {
            int rows = jdbcTemplate.update(sql, token);
            return rows == 1;
        } catch (Exception e) {
            sqlLogger.logError("deletePasswordResetToken", e);
            return false;
        }
    }

    /**
     * Elimina todos los tokens de restablecimiento asociados a un usuario/membresía.
     */
    @Transactional
    public boolean deletePasswordResetTokenByUsername(String username) {
        String sql = "DELETE FROM password_reset_tokens WHERE username = ?";

        try {
            jdbcTemplate.update(sql, username);
            return true;
        } catch (Exception e) {
            sqlLogger.logError("deletePasswordResetTokenByUsername", e);
            return false;
        }
    }
}