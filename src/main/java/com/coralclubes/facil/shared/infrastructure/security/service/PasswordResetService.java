package com.coralclubes.facil.shared.infrastructure.security.service;

import com.coralclubes.facil.shared.infrastructure.exceptions.custom.UsernameNotFound;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.NotificationClient;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import com.coralclubes.facil.shared.infrastructure.security.dto.projection.UserLoginResult;
import com.coralclubes.facil.shared.infrastructure.security.dto.request.PasswordResetConfirmRequest;
import com.coralclubes.facil.shared.infrastructure.security.dto.request.PasswordResetRequest;
import com.coralclubes.facil.shared.infrastructure.security.dto.response.PasswordResetToken;
import com.coralclubes.facil.shared.infrastructure.security.repository.LoginRepository;
import com.coralclubes.facil.shared.infrastructure.security.repository.PasswordResetRepository;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.responses.codes.AuthResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetRepository resetRepo;
    private final LoginRepository loginRepo;
    private final NotificationClient notificationClient;
    private final PasswordEncoder passwordEncoder;
    private final BusinessLogger logger;

    @Value("${app.security.password-reset.expiration-minutes}")
    private Long tokenExpirationMinutes;

    @Value("${app.url.password-reset}")
    private String passwordResetUrl;

    // Configuración para Coral Notificaciones
    @Value("${app.clients.notifications.system-code}")
    private String systemCode;

    @Value("${app.clients.notifications.aliases.default}")
    private String aliasConfig;

    @Value("${app.clients.notifications.templates.reset-password}")
    private String templateCode;


    /**
     * Paso 1: Solicitar Reset.
     */
    public ApiResponse<Boolean> requestPasswordReset(PasswordResetRequest request) {
        // 1. Validar usuario y email
        UserLoginResult user = loginRepo.spLoginUsuarios(request.username()).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (user.email() == null || !user.email().equalsIgnoreCase(request.email())) {
            logger.warn("RESET_PASS", "Email no coincide para usuario {}.", request.username());
            throw new UsernameNotFound("El correo no coincide con el registrado.");
        }

        // 2. Generar Token (generamos un UUID aleatorio como token)
        String token = UUID.randomUUID().toString();

        PasswordResetToken tokenEntity = PasswordResetToken.builder().username(request.username()).email(request.email()).token(token).expiryDate(LocalDateTime.now().plusMinutes(tokenExpirationMinutes)) // agregamos el tiempo de expiración a la hora actual
                .build();

        // 3. Guardar en BD
        resetRepo.savePasswordResetToken(tokenEntity);

        // 4. Enviar Notificación
        sendNotification(user.usuario(), user.email(), token);

        return ApiResponse.success("Se ha enviado un enlace de restablecimiento a su correo electrónico", true);
    }

    /**
     * Paso 2: Validar Token (Cuando el usuario da clic en el link).
     */
    public ApiResponse<Boolean> validateToken(String token) {
        return resetRepo.findPasswordResetToken(token).map(t -> {
            // Validamos que la fecha de expiracion del token no haya pasado
            if (t.expiryDate().isBefore(LocalDateTime.now())) {
                return ApiResponse.<Boolean>error(AuthResponseCode.INVALID_RESET_TOKEN, "El enlace ha expirado.");
            }
            return ApiResponse.<Boolean>success("Token válido", true);
        }).orElse(ApiResponse.<Boolean>error(AuthResponseCode.INVALID_RESET_TOKEN, "Token inválido o no existe."));
    }

    /**
     * Paso 3: Confirmar Cambio.
     */
    public ApiResponse<Boolean> resetPassword(PasswordResetConfirmRequest request) {
        // Validaciones básicas
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        // Buscar y Validar Token
        PasswordResetToken resetToken = resetRepo.findPasswordResetToken(request.token()).orElseThrow(() -> new IllegalArgumentException("Token inválido"));

        if (resetToken.expiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El token ha expirado. Solicite uno nuevo.");
        }

        // Actualizar Password (Encriptado)
        String encodedPassword = passwordEncoder.encode(request.newPassword());
        resetRepo.spLoginCambiarPassword(resetToken.username(), encodedPassword);

        // Quemar Token
        resetRepo.deletePasswordResetToken(request.token());

        // Opcional: Enviar notificación de "Tu contraseña ha sido cambiada exitosamente"
        // sendSuccessNotification(resetToken.email());

        logger.info("RESET_PASS", "Contraseña actualizada para: {}", resetToken.username());
        return ApiResponse.success("Contraseña actualizada correctamente", true);
    }

    // ========================================================================
    // MÉTODOS PRIVADOS DE INTEGRACIÓN
    // ========================================================================

    /**
     * Envía una notificación al usuario con el enlace para restablecer su contraseña utilizando Coral Notificaciones.
     */
    private void sendNotification(String username, String email, String token) {
        String resetLink = passwordResetUrl + "?token=" + token;

        SolicitudNotificacionDto solicitud = SolicitudNotificacionDto.builder()
                .codigoSistema(systemCode)
                .aliasConfig(aliasConfig)
                .destinatarios(List.of(email))
                .codigoPlantilla(templateCode)
                .prioridad(10) // Alta prioridad
                .variables(Map.of("nombreUsuario", username, "resetLink", resetLink, "vigenciaMinutos", tokenExpirationMinutes))
                .build();

        notificationClient.enviarNotificacion(solicitud);
    }
}