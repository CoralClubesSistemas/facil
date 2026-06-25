package com.coralclubes.facil.shared.infrastructure.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.*;

@Component
@Slf4j
public class FirebaseTokenValidator {

    private final JwtDecoder jwtDecoder;

    public FirebaseTokenValidator(@Value("${firebase.project-id}") String projectId) {
        String jwkSetUri = "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com";
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        OAuth2TokenValidator<Jwt> audienceValidator = new JwtAudienceValidator(projectId);
        OAuth2TokenValidator<Jwt> issuerValidator = new JwtIssuerValidator("https://securetoken.google.com/" + projectId);

        // Se pasa la tolerancia de tiempo (Clock Skew) directamente en el constructor
        OAuth2TokenValidator<Jwt> timestampValidator = new JwtTimestampValidator(java.time.Duration.ofMinutes(5));

        java.util.List<OAuth2TokenValidator<Jwt>> validators = java.util.List.of(
                timestampValidator,
                audienceValidator,
                issuerValidator
        );

        OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(validators);

        decoder.setJwtValidator(combinedValidator);
        this.jwtDecoder = decoder;
    }

    public Jwt validarToken(String token) {
        try {
            return jwtDecoder.decode(token);
        } catch (JwtException e) {
            throw new org.springframework.security.authentication.BadCredentialsException("Token de Firebase inválido o expirado", e);
        }
    }

    private static class JwtAudienceValidator implements OAuth2TokenValidator<Jwt> {
        private final String audience;

        public JwtAudienceValidator(String audience) {
            this.audience = audience;
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt jwt) {
            if (jwt.getAudience().contains(audience)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_audience", "La audiencia del token no coincide", null));
        }
    }

    private static class JwtIssuerValidator implements OAuth2TokenValidator<Jwt> {
        private final String issuer;

        public JwtIssuerValidator(String issuer) {
            this.issuer = issuer;
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt jwt) {
            if (jwt.getIssuer() != null && issuer.equals(jwt.getIssuer().toString())) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_issuer", "El emisor del token no coincide", null));
        }
    }
}
