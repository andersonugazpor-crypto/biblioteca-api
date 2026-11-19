package pe.idat.biblioteca.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.biblioteca.config.PropiedadesJwt;
import pe.idat.biblioteca.entidad.RefreshToken;
import pe.idat.biblioteca.entidad.Usuario;
import pe.idat.biblioteca.excepcion.TokenInvalidoException;
import pe.idat.biblioteca.repositorio.RefreshTokenRepositorio;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

/** Emite, valida, rota y revoca refresh tokens. */
@Service
@Transactional
public class RefreshTokenServicio {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final RefreshTokenRepositorio repositorio;
    private final PropiedadesJwt propiedades;
    private final Clock reloj;

    public RefreshTokenServicio(RefreshTokenRepositorio repositorio, PropiedadesJwt propiedades, Clock reloj) {
        this.repositorio = repositorio;
        this.propiedades = propiedades;
        this.reloj = reloj;
    }

    public RefreshToken crear(Usuario usuario) {
        byte[] bytes = new byte[48];
        ALEATORIO.nextBytes(bytes);
        String valor = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expira = Instant.now(reloj).plus(Duration.ofDays(propiedades.refreshExpiracionDias()));
        return repositorio.save(new RefreshToken(valor, usuario, expira));
    }

    /** Valida el token y lo revoca (rotación): cada refresh token sirve una sola vez. */
    public RefreshToken consumir(String valor) {
        RefreshToken token = repositorio.findByToken(valor)
                .orElseThrow(() -> new TokenInvalidoException("Refresh token inválido"));
        if (!token.esValido(Instant.now(reloj))) {
            throw new TokenInvalidoException("El refresh token expiró o fue revocado. Inicie sesión nuevamente");
        }
        if (!token.getUsuario().isActivo()) {
            throw new TokenInvalidoException("La cuenta del usuario está inactiva");
        }
        token.revocar();
        return token;
    }

    public void revocar(String valor) {
        repositorio.findByToken(valor).ifPresent(RefreshToken::revocar);
    }
}
