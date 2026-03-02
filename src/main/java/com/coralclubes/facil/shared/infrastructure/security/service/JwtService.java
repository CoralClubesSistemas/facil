package com.coralclubes.facil.shared.infrastructure.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Utilidad para manejar la generación y validación de JWTs en el Sistema FACIL.
 */
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;
    private final long refreshExpiration;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") Long expiration, @Value("${jwt.refresh-expiration}") Long refreshExpiration) {

        // Decodifica la clave Base64 y crea la llave HMAC (HS256)
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    /**
     * Genera un token JWT firmado.
     *
     * @param username El nombre de usuario para el cual se genera el token.
     *                 Se valida que no sea nulo o vacío para evitar tokens inválidos.
     * @param claims   Un mapa de reclamaciones adicionales que se incluirán en el token.
     * @return El token JWT generado.
     */
    public String generateToken(String username, Map<String, Object> claims) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username no puede ser nulo o vacío");
        }

        return Jwts.builder().claims(claims).subject(username).issuer("facil-core") // Identificador del emisor
                .issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() + expiration)).signWith(secretKey, Jwts.SIG.HS256) // Firma explícita con algoritmo
                .compact();
    }

    /**
     * Genera un token de refresco (Refresh Token).
     *
     * @param username El nombre de usuario para el cual se genera el refresh token.
     * @return El refresh token JWT generado.
     */
    public String generateRefreshToken(String username) {
        return Jwts.builder().subject(username).issuer("facil-refresh").issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() + refreshExpiration)).signWith(secretKey, Jwts.SIG.HS256).compact();
    }

    /**
     * Extrae el nombre de usuario (Subject).
     *
     * @param token El token JWT del cual se extraerá el nombre de usuario.
     * @return El nombre de usuario extraído del token.
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extrae una reclamación específica utilizando un resolver funcional.
     *
     * @param token          El token JWT del cual se extraerá la reclamación.
     * @param claimsResolver Una función que toma las reclamaciones y devuelve el valor deseado.
     * @return El valor de la reclamación extraída del token.
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todas las reclamaciones (Payload) del token.
     *
     * @param token El token JWT del cual se extraerán las reclamaciones.
     * @return Un objeto Claims que contiene todas las reclamaciones del token.
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    /**
     * Valida el token contra los detalles del usuario.
     * Verifica que el nombre de usuario en el token coincida con el del UserDetails
     * y que el token no haya expirado.
     *
     * @param token       El token JWT a validar.
     * @param userDetails Los detalles del usuario para comparar con el token.
     * @return true si el token es válido, false en caso contrario.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Verifica si el token ha expirado.
     * Extrae la fecha de expiración del token y la compara con la fecha actual.
     *
     * @param token El token JWT a verificar.
     * @return true si el token ha expirado, false en caso contrario.
     */
    private boolean isTokenExpired(String token) {
        final Date expirationDate = getClaimFromToken(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }

    /**
     * Verifica si un Refresh Token es válido (formato y expiración).
     *
     * @param refreshToken El refresh token JWT a validar.
     * @return true si el refresh token es válido, false en caso contrario.
     */
    public boolean isRefreshTokenValid(String refreshToken) {
        try {
            // Validar expiración
            if (isTokenExpired(refreshToken)) {
                return false;
            }
            // Validar issuer específico
            final String issuer = getClaimFromToken(refreshToken, Claims::getIssuer);
            return "facil-refresh".equals(issuer);
        } catch (Exception e) {
            return false;
        }
    }
}