package com.coralclubes.facil.shared.infrastructure.security.service;

import com.coralclubes.facil.modules.sistema.dto.response.ModuloApiResponse;
import com.coralclubes.facil.modules.sistema.service.ModulosService;
import com.coralclubes.facil.shared.infrastructure.codes.JwtResponseCode;
import com.coralclubes.facil.shared.infrastructure.codes.LoginResponseCode;
import com.coralclubes.facil.shared.infrastructure.exceptions.custom.NoPermissionsException;
import com.coralclubes.facil.shared.infrastructure.exceptions.custom.NoWebRegistrationException;
import com.coralclubes.facil.modules.sistema.dto.projection.ModuloDtoResult;
import com.coralclubes.facil.shared.infrastructure.exceptions.custom.UsernameNotFound;
import com.coralclubes.facil.shared.infrastructure.security.dto.projection.UserAutorizacionesResult;
import com.coralclubes.facil.shared.infrastructure.security.dto.projection.UserLoginResult;
import com.coralclubes.facil.shared.infrastructure.security.dto.request.LoginRequest;
import com.coralclubes.facil.shared.infrastructure.security.dto.request.RefreshTokenRequest;
import com.coralclubes.facil.shared.infrastructure.security.dto.request.ValidacionAutorizacion;
import com.coralclubes.facil.shared.infrastructure.security.dto.response.*;
import com.coralclubes.facil.shared.infrastructure.security.jwt.JwtService;
import com.coralclubes.facil.shared.infrastructure.security.repository.LoginRepository;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Servicio de Autenticación
 * Maneja el flujo de login mediante SPs y la generación de Tokens JWT.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final LoginRepository loginRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ModulosService modulosService;
    private final PasswordEncoder passwordEncoder;
    private final BusinessLogger logger;

    /**
     * Realiza el login del usuario verificando credenciales vía Stored Procedures.
     */
    public ApiResponse<AuthResponse> login(LoginRequest loginRequest) {
        UserLoginResult userData = loginRepository.spLoginUsuarios(loginRequest.username())
                .orElseThrow(() -> new UsernameNotFound("Usuario no encontrado: " + loginRequest.username()));

        // 2. Validación Manual de Contraseña
        // Si el usuario no tiene pass web (null/empty), lanza la excepción
        if (userData.password() == null || userData.password().isBlank()) {
            throw new NoWebRegistrationException("El usuario no tiene registro web activo: " + userData.usuario());
        }

        // Comparamos el hash que vino de la BD con el texto plano del request
        if (!passwordEncoder.matches(loginRequest.password(), userData.password())) {
            throw new BadCredentialsException("Contraseña incorrecta");
        }

        // 3. Obtener Autorizaciones (Lógica de Negocio)
        List<UserAutorizacionesResult> autorizaciones = loginRepository.spLoginObtenerAutorizacionesUsuario(userData.usuario());

        if (autorizaciones.isEmpty()) {
            throw new NoPermissionsException("El usuario no tiene permisos asignados.");
        }

        // 4. Generar Tokens
        String token = generateToken(userData.usuario(), autorizaciones);
        String refreshToken = jwtService.generateRefreshToken(userData.usuario());

        logger.info(userData.usuario(),"Usuario '{}' autenticado exitosamente", userData.usuario());

        // 5. Retornar
        return ApiResponse.from(LoginResponseCode.LOGIN_SUCCESS, buildAuthResponse(token, refreshToken, userData));
    }

    // Genera un token JWT con las autorizaciones del usuario como reclamaciones adicionales.
    private String generateToken(String username, List<UserAutorizacionesResult> autorizaciones) {
        Map<String, Object> claims = new HashMap<>();
        List<String> authClaves = autorizaciones.stream().map(UserAutorizacionesResult::clave).filter(Objects::nonNull).toList(); // Java 17+ toList()

        claims.put("auth", authClaves);
        return jwtService.generateToken(username, claims);
    }

    // Construye la respuesta de autenticación con el token, refresh token y datos adicionales del usuario.
    private AuthResponse buildAuthResponse(String token, String refreshToken, UserLoginResult user) {
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .usuario(user.usuario())
                .idDesarrollo(user.idDesarrollo())
                .email(user.email())
                .rolId(user.rolId())
                .rolDescripcion(user.rolDescripcion())
                .build();
    }

    /**
     * Renueva el token de acceso usando un Refresh Token válido.
     */
    public ApiResponse<RefreshTokenResponse> refreshToken(RefreshTokenRequest request) {
        if (!jwtService.isRefreshTokenValid(request.refreshToken())) {
            throw new BadCredentialsException("Token de refresco inválido o expirado");
        }

        String username = jwtService.getUsernameFromToken(request.refreshToken());
        UserLoginResult userData = loginRepository.spLoginUsuarios(username).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        List<UserAutorizacionesResult> autorizaciones = loginRepository.spLoginObtenerAutorizacionesUsuario(username);
        if (autorizaciones.isEmpty()) {
            throw new NoPermissionsException("El usuario perdió sus permisos. Re-autentique.");
        }

        String newToken = generateToken(userData.usuario(), autorizaciones);
        String newRefreshToken = jwtService.generateRefreshToken(userData.usuario());

        return ApiResponse.from(JwtResponseCode.JWT_REFRESH_SUCCESS, new RefreshTokenResponse(newToken, newRefreshToken));
    }

    /**
     * Obtiene los módulos asignados a un usuario por su nombre de usuario con base en su rol.
     * Si el usuario no tiene módulos asignados, se lanza una excepción personalizada.
     *
     * @param username Nombre de usuario a buscar.
     * @return Lista de ModuloApiResponse con los módulos asignados al usuario.
     */
    public ApiResponse<List<ModuloApiResponse>> getModulosUsuario(String username) {
        List<ModuloDtoResult> userResults = loginRepository.spLoginModulosUsuarios(username);
        if (userResults.isEmpty()) {
            throw new NoPermissionsException("El usuario no tiene módulos asignados");
        }
        return ApiResponse.from(LoginResponseCode.LOGIN_MODULES_CONSTRAINT, modulosService.getFormatModules(userResults));
    }

    /**
     * Validación de login simple (sin contexto de seguridad completo) usando BCrypt nativo.
     */
    public ApiResponse<Boolean> validarLoginSimple(LoginRequest loginRequest) {
        if (loginRequest.username().isBlank() || loginRequest.password().isBlank()) {
            throw new IllegalArgumentException("Credenciales incompletas");
        }

        LoginRequest userStored = Optional.ofNullable(loginRepository.spLoginSimple(loginRequest.username())).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        boolean isValid = passwordEncoder.matches(loginRequest.password(), userStored.password());
        if (!isValid) throw new BadCredentialsException("Contraseña incorrecta");

        return ApiResponse.from(LoginResponseCode.LOGIN_SUCCESS, true);
    }

    /**
     * Valida si una autorización específica es válida para un usuario, sin necesidad de un contexto de seguridad completo.
     * <p>
     * Este método es útil para validaciones puntuales de autorizaciones en casos donde no se requiere toda la información del usuario o el contexto de seguridad, como en ciertos endpoints públicos o para validaciones internas.
     *
     * @param request Objeto que contiene el nombre de usuario, contraseña y la autorización a validar.
     * @return ApiResponse con un booleano indicando si la autorización es válida o no.
     */
    public ApiResponse<Boolean> validarAutorizacionFueraDePolitica(ValidacionAutorizacion request) {
        validarLoginSimple(new LoginRequest(request.username(), request.password()));

        int validado = loginRepository.spLoginValidarAutorizacionFueraDePolitica(request.username(), request.autorizacion());
        boolean esValido = validado == 1;

        return ApiResponse.from(esValido ? LoginResponseCode.LOGIN_AUTHORIZATION_SUCCESS : LoginResponseCode.LOGIN_NOT_PERMITIONS, esValido);
    }
}