package com.coralclubes.facil.modules.clientes.service;

import com.coralclubes.facil.modules.clientes.dto.projection.ClienteValidacionMembresiaResult;
import com.coralclubes.facil.modules.clientes.dto.request.ClienteRegistroRequest;
import com.coralclubes.facil.modules.clientes.dto.response.ValidacionCorreoDto;
import com.coralclubes.facil.modules.clientes.repository.ClientesRepository;
import com.coralclubes.facil.modules.usuarios.dto.response.PasswordResetToken;
import com.coralclubes.facil.modules.usuarios.repository.PasswordResetRepository;
import com.coralclubes.facil.modules.usuarios.repository.UsuariosRepository;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.NotificationClient;
import com.coralclubes.facil.shared.infrastructure.integration.notifications.dto.SolicitudNotificacionDto;
import com.coralclubes.facil.shared.utils.VerificationCodeGenerator;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.responses.codes.AuthResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientesRegistrationService {

    private final ClientesRepository clientesRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationClient notificationClient;
    private final PasswordResetRepository passRepo;

    @Value("${app.clients.notifications.aliases.default}")
    private String defaultAlias;

    public Boolean registroValido(String membresia, String email) {
        ClienteValidacionMembresiaResult result = validarMembresia(membresia, email);
        Boolean isValid = !result.registroUserSystem();
        String nombreCompleto = result.nombreCompleto();

        log.debug("Validación de registro para membresía {} y correo {}: {}", membresia, email, isValid);

        if (isValid) {
            // obtenemos el codigo de verificacion
            String code = VerificationCodeGenerator.generateNumericCode(5);

            LocalDateTime expirationTime = LocalDateTime.now().plusDays(1); // Expira en 1 día

            // guardamos en la base e datos
            // 3. Guardar en BD
            passRepo.savePasswordResetToken(new PasswordResetToken(membresia, email, code, expirationTime));

            // Creamos el cuerpo de la solicitud de notificación
            var solicitud = SolicitudNotificacionDto.builder()
                    .aliasConfig(defaultAlias)
                    .destinatarios(List.of(email))
                    .codigoPlantilla("codigo-verificacion-v1")
                    .variables(
                            Map.of(
                                    "codigoVerificacion", code,
                                    "nombreUsuario", nombreCompleto
                            )
                    )
                    .metadatos(Map.of(
                            "MODULO", "CLIENTES",
                            "MEMBRESIA", membresia,
                            "CODIGO", code
                    ))
                    .prioridad(1)
                    .build();

            // enviamos la notificación
            notificationClient.enviarNotificacion(solicitud);
        }

        return isValid;
    }

    public Boolean validarCodigoVerificacion(String membresia, String codigo) {
        PasswordResetToken token = passRepo.findPasswordResetToken(codigo)
                .orElseThrow(() -> new BadCredentialsException("El código de verificación no es válido o ha expirado."));

        if (!token.username().equals(membresia)) {
            throw new BadCredentialsException("El código de verificación no corresponde a la membresía proporcionada.");
        }

        // Si el código es válido, podemos eliminarlo de la base de datos para evitar reutilización
        passRepo.deletePasswordResetToken(codigo);

        return true;
    }

    private ClienteValidacionMembresiaResult validarMembresia(String membresia, String email) {
        return clientesRepository.spClientesValidarMembresia(membresia, email)
                .orElseThrow(() -> new BadCredentialsException("Membresía no encontrada"));
    }

    public void crearAccesoWeb(ClienteRegistroRequest request) {
        // 1. Validar que la membresía exista
        ClienteValidacionMembresiaResult validacion = validarMembresia(request.membresia(), request.correo());

        // 2. Validar que no tenga ya un registro activo
        if (Boolean.TRUE.equals(validacion.registroUserSystem())) {
            throw new IllegalArgumentException("La membresía ya cuenta con un acceso web activo.");
        }

        String passwordHash = null;
        // 3. Hashear contraseña
        if (request.password() != null && !request.password().isBlank()) {
            passwordHash = passwordEncoder.encode(request.password());
        }

        // 4. Crear acceso web
        clientesRepository.spClientesCrearAccesoWeb(
                request.membresia(),
                request.correo(),
                passwordHash,
                request.tokenProveedor()
        );
        log.debug("Acceso web creado para membresía {} con correo {}, contraseña: {} y token proveedor {}", request.membresia(), request.correo(), passwordHash, request.tokenProveedor());
        log.info("Acceso web creado con éxito para la membresía: {}", request.membresia());
    }

    public Boolean reenviarCodigo(String membresia, String email) {
        // 1. Validar membresía y verificar que no tenga un registro activo
        ClienteValidacionMembresiaResult validacion = validarMembresia(membresia, email);
        if (Boolean.TRUE.equals(validacion.registroUserSystem())) {
            throw new IllegalArgumentException("La membresía ya cuenta con un acceso web activo.");
        }

        // 2. Eliminar de la base de datos el token anterior si existe
        passRepo.deletePasswordResetTokenByUsername(membresia);

        // 3. Generar nuevo código de verificación
        String code = VerificationCodeGenerator.generateNumericCode(5);
        LocalDateTime expirationTime = LocalDateTime.now().plusDays(1);

        // 4. Guardar en BD
        passRepo.savePasswordResetToken(new PasswordResetToken(membresia, email, code, expirationTime));

        // 5. Enviar notificación
        var solicitud = SolicitudNotificacionDto.builder()
                .aliasConfig(defaultAlias)
                .destinatarios(List.of(email))
                .codigoPlantilla("codigo-verificacion-v1")
                .variables(
                        Map.of(
                                "codigoVerificacion", code,
                                "nombreUsuario", validacion.nombreCompleto()
                        )
                )
                .metadatos(Map.of(
                        "MODULO", "CLIENTES",
                        "MEMBRESIA", membresia,
                        "CODIGO", code
                ))
                .prioridad(1)
                .build();

        notificationClient.enviarNotificacion(solicitud);
        log.info("Código de verificación reenviado y guardado para la membresía: {}", membresia);

        return true;
    }

    public ValidacionCorreoDto validarCorreoExistente(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio.");
        }
        return clientesRepository.spUsuariosValidarCorreoExistente(correo.trim())
                .orElse(new ValidacionCorreoDto(false, false, false));
    }
}
