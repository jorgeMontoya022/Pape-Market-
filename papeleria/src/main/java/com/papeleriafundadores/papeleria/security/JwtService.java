package com.papeleriafundadores.papeleria.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Servicio responsable de generar, parsear y validar tokens JWT.
 *
 * Usa la librería jjwt 0.12.x con HMAC-SHA256.
 * La clave secreta y el tiempo de expiración se leen desde application.properties
 * (que a su vez los obtiene del archivo .env).
 */
@Service
public class JwtService {

    /** Clave secreta inyectada desde la propiedad jwt.secret */
    @Value("${jwt.secret}")
    private String secretKey;

    /** Tiempo de vida del token en milisegundos (por defecto 24 horas) */
    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    // ---------------------------------------------------------------
    // API pública
    // ---------------------------------------------------------------

    /**
     * Genera un token JWT firmado con HMAC-SHA256.
     *
     * @param email email del usuario autenticado (subject del token)
     * @param role  nombre del rol (e.g. "ADMIN", "VENDEDOR") — se incluye como claim
     * @return token JWT en formato compacto: header.payload.signature
     */
    public String generateToken(String email, String role) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claims(Map.of("role", role))   // claim adicional con el rol
                .issuedAt(now)
                .expiration(expiry)
                .signWith(buildKey())           // firma con HMAC-SHA256
                .compact();
    }

    /**
     * Extrae el email (subject) de un token JWT.
     * Lanza JwtException si el token es inválido o está expirado.
     *
     * @param token token JWT compacto
     * @return email del usuario
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Valida que el token sea auténtico, no esté expirado y que el subject
     * coincida con el email del usuario esperado.
     *
     * @param token token JWT compacto
     * @param email email con el que se compara el subject
     * @return true si el token es válido y pertenece a ese email
     */
    public boolean isTokenValid(String token, String email) {
        try {
            String subject = extractEmail(token);
            return subject.equals(email) && !isExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            // Token malformado, firma incorrecta, expirado, etc.
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Helpers privados
    // ---------------------------------------------------------------

    /** Parsea el token y devuelve el cuerpo de claims. Lanza JwtException si falla. */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(buildKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Construye la SecretKey HMAC-SHA256 a partir de la cadena hexadecimal del .env */
    private SecretKey buildKey() {
        // Keys.hmacShaKeyFor exige al menos 256 bits (32 bytes).
        // Nuestro secreto es 64 caracteres hex = 32 bytes → cumple el requisito.
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /** Comprueba si la fecha de expiración del token ya pasó. */
    private boolean isExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }
}
