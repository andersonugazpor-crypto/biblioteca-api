package pe.idat.biblioteca.dto;

/** Respuesta de login, registro y refresco: access token (JWT) + refresh token. */
public record AuthResponse(
        String tipoToken,
        String accessToken,
        long expiraEnSegundos,
        String refreshToken,
        UsuarioResponse usuario) {
}
