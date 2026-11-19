package pe.idat.biblioteca.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import pe.idat.biblioteca.config.PropiedadesJwt;
import pe.idat.biblioteca.entidad.Usuario;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Genera y valida los access tokens (JWT firmados con HMAC-SHA256).
 * Claims: sub = email, uid = id del usuario, roles, tipo = "access", iss, iat, exp, jti.
 */
@Service
public class JwtServicio {

    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_TIPO = "tipo";
    public static final String TIPO_ACCESS = "access";

    private final SecretKey clave;
    private final PropiedadesJwt propiedades;
    private final Clock reloj;

    public JwtServicio(PropiedadesJwt propiedades, Clock reloj) {
        this.propiedades = propiedades;
        this.reloj = reloj;
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(propiedades.secreto()));
    }

    public String generarAccessToken(Usuario usuario) {
        Instant ahora = Instant.now(reloj);
        List<String> roles = usuario.getRoles().stream().map(r -> r.getNombre().name()).sorted().toList();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(propiedades.emisor())
                .subject(usuario.getEmail())
                .claim("uid", usuario.getId())
                .claim(CLAIM_ROLES, roles)
                .claim(CLAIM_TIPO, TIPO_ACCESS)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(Duration.ofMinutes(propiedades.expiracionMinutos()))))
                .signWith(clave)
                .compact();
    }

    /**
     * Verifica firma, emisor y expiración. Lanza {@link io.jsonwebtoken.ExpiredJwtException}
     * o {@link io.jsonwebtoken.JwtException} si el token no es válido.
     */
    public Claims validar(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .requireIssuer(propiedades.emisor())
                .clock(() -> Date.from(Instant.now(reloj)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long expiracionSegundos() {
        return propiedades.expiracionMinutos() * 60;
    }
}
