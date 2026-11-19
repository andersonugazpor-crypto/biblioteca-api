package pe.idat.biblioteca.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de tokens (prefijo "jwt").
 *
 * @param secreto              clave HMAC-SHA256 en Base64 (mínimo 256 bits)
 * @param expiracionMinutos    vida del access token
 * @param refreshExpiracionDias vida del refresh token
 * @param emisor               valor del claim "iss"
 */
@ConfigurationProperties(prefix = "jwt")
public record PropiedadesJwt(String secreto, long expiracionMinutos, long refreshExpiracionDias, String emisor) {
}
