package pe.idat.biblioteca.servicio;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.biblioteca.dto.AuthResponse;
import pe.idat.biblioteca.dto.LoginRequest;
import pe.idat.biblioteca.dto.RefreshRequest;
import pe.idat.biblioteca.dto.RegistroRequest;
import pe.idat.biblioteca.entidad.NombreRol;
import pe.idat.biblioteca.entidad.RefreshToken;
import pe.idat.biblioteca.entidad.Usuario;
import pe.idat.biblioteca.mapper.DtoMapper;
import pe.idat.biblioteca.seguridad.JwtServicio;

import java.util.Set;

/** Login, registro, refresco y cierre de sesión. */
@Service
@Transactional
public class AuthServicio {

    private static final String TIPO = "Bearer";

    private final AuthenticationManager authenticationManager;
    private final UsuarioServicio usuarios;
    private final JwtServicio jwt;
    private final RefreshTokenServicio refreshTokens;

    public AuthServicio(AuthenticationManager authenticationManager, UsuarioServicio usuarios, JwtServicio jwt,
                        RefreshTokenServicio refreshTokens) {
        this.authenticationManager = authenticationManager;
        this.usuarios = usuarios;
        this.jwt = jwt;
        this.refreshTokens = refreshTokens;
    }

    /** Lanza BadCredentialsException o DisabledException si falla (las maneja el GlobalExceptionHandler). */
    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.password()));
        return emitir(usuarios.buscarPorEmail(email));
    }

    public AuthResponse registrar(RegistroRequest req) {
        Usuario usuario = usuarios.registrar(req.nombres(), req.apellidos(), req.email(), req.codigoUniversitario(),
                req.password(), Set.of(NombreRol.USUARIO));
        return emitir(usuario);
    }

    /** Rotación: el refresh token usado queda revocado y se entrega uno nuevo. */
    public AuthResponse refrescar(RefreshRequest req) {
        RefreshToken usado = refreshTokens.consumir(req.refreshToken());
        return emitir(usado.getUsuario());
    }

    public void logout(RefreshRequest req) {
        refreshTokens.revocar(req.refreshToken());
    }

    private AuthResponse emitir(Usuario usuario) {
        return new AuthResponse(TIPO, jwt.generarAccessToken(usuario), jwt.expiracionSegundos(),
                refreshTokens.crear(usuario).getToken(), DtoMapper.usuario(usuario));
    }
}
