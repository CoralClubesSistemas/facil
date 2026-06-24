package com.coralclubes.facil.shared.infrastructure.gateway.service;

import com.coralclubes.facil.modules.clientes.dto.projection.ClienteLoginResult;
import com.coralclubes.facil.modules.clientes.dto.projection.ClienteValidacionMembresiaResult;
import com.coralclubes.facil.modules.clientes.repository.ClientesRepository;
import com.coralclubes.facil.shared.infrastructure.exceptions.custom.NoWebRegistrationException;
import com.coralclubes.facil.shared.infrastructure.gateway.controller.ClientesInternalAuthController.ClientLoginRequest;
import com.coralclubes.facil.shared.infrastructure.gateway.dto.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientesGatewayAuthService {

    private final ClientesRepository clientesRepository;
    private final PasswordEncoder passwordEncoder;
    private final FirebaseTokenValidator firebaseTokenValidator;

    public UserInfo autenticarCliente(ClientLoginRequest request) {
        String membresia;

        if (request.token() != null && !request.token().isBlank()) {
            membresia = autenticarConFirebase(request.token());
        } else {
            membresia = autenticarConPassword(request.email(), request.password());
        }

        // Obtener información complementaria de la membresía
        ClienteValidacionMembresiaResult membresiaInfo = clientesRepository.spClientesValidarMembresia(membresia, request.email())
                .orElse(null);

        Integer idDesarrollo = (membresiaInfo != null) ? membresiaInfo.desarrollo() : null;
        String desarrolloDescripcion = (membresiaInfo != null) ? membresiaInfo.descripcionDesarrollo() : null;

        String correo = null;
        if (membresiaInfo != null) {
            correo = membresiaInfo.correoPersonal() != null ? membresiaInfo.correoPersonal() : membresiaInfo.correoTrabajo();
        }

        return UserInfo.builder()
                .username(membresia)
                .email(correo)
                .nombreCompleto(membresiaInfo != null ? membresiaInfo.nombreCompleto() : null)
                .rolId(null)
                .role("CLIENTE")
                .source("EXTERNAL")
                .legacyId(membresia)
                .status("REGISTERED")
                .idDesarrollo(idDesarrollo)
                .desarrolloDescripcion(desarrolloDescripcion)
                .permissions(Collections.emptyList())
                .build();
    }

    private String autenticarConFirebase(String token) {
        log.info("Iniciando validación de Firebase Token");
        Jwt jwt = firebaseTokenValidator.validarToken(token);

        String firebaseUid = jwt.getSubject();
        if (firebaseUid == null || firebaseUid.isBlank()) {
            throw new BadCredentialsException("El token de Firebase no contiene un identificador de usuario válido (subject)");
        }

        log.debug("Token de Firebase decodificado con éxito. UID: {}", firebaseUid);

        String membresia = clientesRepository.buscarMembresiaPorFirebaseUid(firebaseUid)
                .orElseThrow(() -> new BadCredentialsException("No se encontró una membresía activa vinculada a este acceso de Firebase"));

        log.info("Firebase token validado con éxito para membresía: {} (UID: {})", membresia, firebaseUid);
        return membresia;
    }

    private String autenticarConPassword(String email, String password) {
        ClienteLoginResult loginData = clientesRepository.spClientesLogin(email)
                .orElseThrow(() -> new BadCredentialsException("Membresía o contraseña incorrecta"));

        if (loginData.passwordHash() == null || loginData.passwordHash().isBlank()) {
            throw new NoWebRegistrationException("El cliente no tiene un registro web activo");
        }

        if (!passwordEncoder.matches(password, loginData.passwordHash())) {
            throw new BadCredentialsException("Membresía o contraseña incorrecta");
        }

        return loginData.membresia();
    }

    public UserInfo obtenerPorMembresia(String membresia) {
        ClienteLoginResult loginData = clientesRepository.spClientesLogin(membresia)
                .orElseThrow(() -> new BadCredentialsException("Membresía no encontrada: " + membresia));

        ClienteValidacionMembresiaResult membresiaInfo = clientesRepository.spClientesValidarMembresia(membresia, loginData.correo())
                .orElse(null);

        Integer idDesarrollo = (membresiaInfo != null) ? membresiaInfo.desarrollo() : null;
        String desarrolloDescripcion = (membresiaInfo != null) ? membresiaInfo.descripcionDesarrollo() : null;

        assert membresiaInfo != null;
        return UserInfo.builder()
                .username(loginData.membresia())
                .email(loginData.correo())
                .nombreCompleto(membresiaInfo.nombreCompleto())
                .rolId(null)
                .role("CLIENTE")
                .source("EXTERNAL")
                .legacyId(loginData.membresia())
                .status("REGISTERED")
                .idDesarrollo(idDesarrollo)
                .desarrolloDescripcion(desarrolloDescripcion)
                .permissions(Collections.emptyList())
                .build();
    }
}
