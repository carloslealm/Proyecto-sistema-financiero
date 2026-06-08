package com.loanmanager.backend.util;
 
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
 
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
 
// @Slf4j genera automáticamente: private static final Logger log = ...
// Usamos log.info(), log.debug(), log.error() para trazabilidad.
@Slf4j
// @Component: Spring gestiona esta clase como un Bean.
// La diferencia con @Service es semántica:
//   @Component → utilitario genérico
//   @Service   → lógica de negocio
//   @Repository → acceso a datos
// Todos son @Component por debajo, pero el nombre comunica intención.
@Component
public class JwtUtil {
 
    // @Value inyecta valores del application.yml.
    // Si el valor no existe en la config, Spring lanza error al arrancar.
    @Value("${jwt.secret}")
    private String secret;
 
    @Value("${jwt.expiration}")
    private Long expirationMs;
 
    // ── Generación del token ──────────────────────────────────
 
    public String generarToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Guardamos información adicional dentro del token (payload).
        // El frontend puede decodificar el token y leer estos datos
        // sin hacer otro request al servidor.
        // NUNCA guardes información sensible en el token —
        // es codificado en Base64 pero NO encriptado.
        claims.put("email", userDetails.getUsername());
        claims.put("roles", userDetails.getAuthorities().stream()
            .map(a -> a.getAuthority())
            .toList());
        return construirToken(claims, userDetails.getUsername());
    }
 
    private String construirToken(Map<String, Object> claims, String subject) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);
 
        return Jwts.builder()
            .claims(claims)
            // subject: identificador principal del token (email del usuario)
            .subject(subject)
            // iat: "issued at" — cuándo fue creado
            .issuedAt(ahora)
            // exp: cuándo expira (ahora + 24h)
            .expiration(expiracion)
            // Firmamos con HMAC-SHA256 usando nuestra clave secreta.
            // Si alguien modifica el payload, la firma no coincide
            // y el token es rechazado. Aquí está la seguridad real del JWT.
            .signWith(getSigningKey())
            .compact();
    }
 
    // ── Validación del token ──────────────────────────────────
 
    public boolean esTokenValido(String token, UserDetails userDetails) {
        try {
            final String email = extraerEmail(token);
            return email.equals(userDetails.getUsername()) && !estaExpirado(token);
        } catch (JwtException e) {
            log.error("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }
 
    public boolean estaExpirado(String token) {
        return extraerExpiracion(token).before(new Date());
    }
 
    // ── Extracción de datos del token ─────────────────────────
 
    public String extraerEmail(String token) {
        return extraerClaim(token, Claims::getSubject);
    }
 
    public Date extraerExpiracion(String token) {
        return extraerClaim(token, Claims::getExpiration);
    }
 
    // Método genérico con Function<Claims, T>.
    // Permite extraer cualquier claim con una sola implementación.
    // Ejemplo: extraerClaim(token, claims -> claims.get("roles"))
    public <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraerTodosClaims(token);
        return claimsResolver.apply(claims);
    }
 
    private Claims extraerTodosClaims(String token) {
        // parseSignedClaims verifica la firma del token.
        // Si alguien modificó el token, lanza SignatureException.
        // Si el token expiró, lanza ExpiredJwtException.
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
 
    private SecretKey getSigningKey() {
        // Convertimos el String del application.yml a una clave criptográfica.
        // Keys.hmacShaKeyFor garantiza que la clave sea válida para HMAC-SHA.
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}