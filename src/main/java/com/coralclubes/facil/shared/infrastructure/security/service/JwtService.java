package com.coralclubes.facil.shared.infrastructure.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * Utilidad para VALIDAR tokens JWT generados por el API Gateway.
 *
 * Este servicio NO genera tokens — eso lo hace el gateway.
 * Solo se usa para:
 * - Validar tokens en conexiones WebSocket (STOMP)
 * - Extraer claims del JWT cuando es necesario
 *
 * La clave secreta DEBE ser idéntica a la del gateway.
 */
@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el username (subject) del token.
     * Retorna null si el token es inválido o expirado.
     */
    public String extractUsername(String token) {
        try {
            Claims claims = extractAllClaims(token);
            if (isExpired(claims)) return null;
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Crea un objeto Authentication a partir del token JWT.
     * Usado por el WebSocketConfig para autenticar conexiones STOMP.
     */
    public Authentication getAuthentication(String token) {
        String username = extractUsername(token);
        return new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
    }

    /**
     * Extrae los permisos del JWT.
     * El gateway los pone como claim "permissions" separado por comas.
     */
    public List<String> extractPermissions(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String permissions = claims.get("permissions", String.class);
            if (permissions == null || permissions.isBlank()) {
                return Collections.emptyList();
            }
            return List.of(permissions.split(","));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Valida si un token es válido (no expirado, firma correcta).
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !isExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrae un claim genérico del token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.before(new Date());
    }
}
